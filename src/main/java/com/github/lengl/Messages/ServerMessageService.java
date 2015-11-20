package com.github.lengl.Messages;

import com.github.lengl.Authorization.AuthorisationService;
import com.github.lengl.Authorization.AuthorisationServiceResponse;
import com.github.lengl.ChatRoom.ChatRoom;
import com.github.lengl.Messages.ServerMessages.AuthMessage;
import com.github.lengl.Messages.ServerMessages.ChatCreatedMessage;
import com.github.lengl.Messages.ServerMessages.QuitMessage;
import com.github.lengl.Messages.ServerMessages.ResponseMessage;
import com.github.lengl.Users.User;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ServerMessageService implements InputHandler {

  private static final String UNAUTHORIZED =
      "You need to authorise (/login) or to register (/signin) yourself to use this command.";
  private final Logger log = Logger.getLogger(ServerMessageService.class.getName());
  private final AuthorisationService authorisationService;
  private MessageStorable historyStorage = null;
  private User authorizedUser = null;

  public ServerMessageService(@NotNull AuthorisationService authorisationService) {
    this.authorisationService = authorisationService;
  }

  @Nullable
  public Message react(@NotNull Message message) {
    String trimmed = message.getBody().trim();
    //this means return will be server response and has no author.
    if (trimmed.startsWith("/")) {
      //login as smone
      if (trimmed.startsWith("/login")) {
        return new AuthMessage(handleLogin(trimmed), authorizedUser);
      }

      //sign in
      if (trimmed.startsWith("/signin")) {
        return new AuthMessage(handleSignin(trimmed), authorizedUser);
      }

      //print help message
      if ("/help".equals(trimmed)) {
        return new ResponseMessage(
            "/help\n" +
                "/login <login> <password>\n" +
                "/signin <login> <password>" +
                "/user <nickname>\n" +
                "/history <amount>\n" +
                "/quit");
      }

      //change user nickname
      if (trimmed.startsWith("/user")) {
        return new ResponseMessage(handleNickname(trimmed));
      }

      //print user's message history
      if (trimmed.startsWith("/history")) {
        return new ResponseMessage(handleHistory(trimmed));
      }

      //find Messages matching regex
      if (trimmed.startsWith("/find")) {
        return new ResponseMessage(handleFind(trimmed));
      }

      if (trimmed.startsWith("/chat_create")) {
        return handleChatCreate(trimmed);
      }

      //finish sending Messages
      if ("/q".equals(trimmed) || "/quit".equals(trimmed)) {
        return new QuitMessage("Goodbye!");
      }
    }

    //This means we received general message
    if (historyStorage != null) {
      historyStorage.addMessage(message);
    }
    if (authorizedUser != null) {
      message.setAuthor(authorizedUser.getNickname());
    } else {
      message.setAuthor("unknownUser" + message.getSenderId());
    }
    return message;
  }

  @NotNull
  private String handleSignin(@NotNull String trimmed) {
    int OFFSET = 7; //length of string /signin
    String userAndPwd = trimmed.substring(OFFSET).trim();
    String[] parsed = userAndPwd.trim().split(" ", 2);
    if (parsed.length == 1) {
      return "Usage: /signin <your login> <your password>";
    }
    try {
      AuthorisationServiceResponse response = authorisationService.createNewUserAndAuthorise(parsed[0], parsed[1]);
      if (response.user != null) {
        authorizedUser = response.user;
        historyStorage = new MessageFileStorage(authorizedUser);
      }
      return response.response;
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
      return "Usage: /signin <your login> <your password>";
    } catch (Exception e) {
      log.log(Level.SEVERE, "Exception: ", e);
      return "Unable to sign in. Please try again later";
    }
  }

  @NotNull
  private String handleLogin(@NotNull String trimmed) {
    int OFFSET = 6; //length of string "/login"
    String userAndPwd = trimmed.substring(OFFSET).trim();
    String[] parsed = userAndPwd.trim().split(" ", 2);
    if (parsed.length == 1) {
      return "Usage: /login <your login> <your password>";
    }
    try {
      AuthorisationServiceResponse response = authorisationService.authorize(parsed[0], parsed[1]);
      if (response.user != null) {
        authorizedUser = response.user;
        historyStorage = new MessageFileStorage(authorizedUser);
      }
      return response.response;
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
      return "Usage: /login <your login> <your password>";
    } catch (Exception e) {
      log.log(Level.SEVERE, "Exception: ", e);
      return "Unable to login. Please try again later";
    }
  }

  @NotNull
  private String handleNickname(@NotNull String trimmed) {
    //TODO: Probably check if nickname already used & give it a number (e.g. lengl, lengl2, lengl3...)
    //TODO: Save nickname to UserStore!!!
    int OFFSET = 5; //length of string "/user"
    if (authorizedUser != null) {
      authorizedUser.setNickname(trimmed.substring(OFFSET).trim());
      return "Username " + trimmed.substring(OFFSET).trim() + " set successfully.";
    } else {
      return UNAUTHORIZED;
    }
  }

  @NotNull
  private String handleHistory(@NotNull String trimmed) {
    int OFFSET = 8; //length of string "/history"
    if (authorizedUser != null) {
      String history;
      if ("/history".equals(trimmed)) {
        history = historyStorage.getHistory(0);
      } else {
        try {
          history = historyStorage.getHistory(Integer.parseInt(trimmed.substring(OFFSET).trim()));
        } catch (NumberFormatException ex) {
          log.info("Wrong input parameter caught for \"history\"");
          return "Usage: /history <quantity> or /history";
        }
      }
      return history;
    } else {
      return UNAUTHORIZED;
    }
  }

  @NotNull
  private String handleFind(@NotNull String trimmed) {
    int OFFSET = 5; //length of string "/find"
    if (authorizedUser != null) {
      try {
        String regex = trimmed.substring(OFFSET).trim();
        Pattern.compile(regex);
        return historyStorage.findMessage(regex);
      } catch (PatternSyntaxException e) {
        log.info("Wrong input parameter caught for \"find\"");
        return "Invalid regular expression";
      }
    } else {
      return UNAUTHORIZED;
    }
  }

  @NotNull
  private Message handleChatCreate(@NotNull String trimmed) {
    if (authorizedUser != null) {
      int OFFSET = "/chat_create".length();
      String[] userlist = trimmed.substring(OFFSET).trim().split(",");
      ChatRoom room;
      try {
        room = new ChatRoom();
      } catch (IOException e) {
        log.info("Chat room can not be created");
        return new ResponseMessage("Chat room can not be created");
      }
      StringBuilder stringBuilder = new StringBuilder();
      for (String userId : userlist) {
        try {
          User foundUser = authorisationService.userStorage.findUserById(Integer.parseInt(userId));
          if (foundUser != null)
            room.addParticipant(foundUser);
          else
            stringBuilder.append("User ").append(userId).append(" not found\n");
        } catch (NumberFormatException ex) {
          log.info("Wrong input parameter for chatCreate");
          return new ResponseMessage("Usage: /chat_create <user_id>, <user_id>, ...");
        } catch (Exception e) {
          log.log(Level.SEVERE, "Exception: ", e);
          return new ResponseMessage("Unable to create chat. Please try again later");
        }
      }
      room.addParticipant(authorizedUser);
      stringBuilder.append("Chat ").append(room.getId()).append(" created successfully.");
      return new ChatCreatedMessage(stringBuilder.toString(), room);
    } else {
      return new ResponseMessage(UNAUTHORIZED);
    }
  }

  public void stop() {
    if (historyStorage != null)
      historyStorage.close();
  }
}
