package com.github.lengl.Messages;

import com.github.lengl.Authorization.AuthorisationService;
import com.github.lengl.Authorization.AuthorisationServiceResponse;
import com.github.lengl.ChatRoom.ChatRoomStorable;
import com.github.lengl.Messages.ServerMessages.AuthMessage;
import com.github.lengl.Messages.ServerMessages.QuitMessage;
import com.github.lengl.Messages.ServerMessages.ResponseMessage;
import com.github.lengl.Users.User;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ServerMessageService implements InputHandler {
  private static final String HELP =
      "/help\n" +
          "/chat_create <user_id>, <user_id>, ...\n" +
          "/chat_find <chat_id> <regex>\n" +
          "/chat_history <chat_id> <amount>\n" +
          "/chat_list\n" +
          "/chat_send <chat_id> <message>\n" +
          "/find <regex>\n" +
          "/history <amount>\n" +
          "/login <login> <password>\n" +
          "/signin <login> <password>\n" +
          "/user <nickname>\n" +
          "/user_info <user_id>\n" +
          "/user_pass <old password> <new password>\n" +
          "/quit";
  private static final String UNAUTHORIZED =
      "You need to authorise (/login) or to register (/signin) yourself to use this command.";
  private final Logger log = Logger.getLogger(ServerMessageService.class.getName());
  private final AuthorisationService authorisationService;
  private ChatRoomStorable chatRoomDBStorage;
  private MessageStorable historyStorage = null;
  private User authorizedUser = null;

  public ServerMessageService(@NotNull AuthorisationService authorisationService) {
    this.authorisationService = authorisationService;
  }

  public ServerMessageService(@NotNull AuthorisationService authorisationService, @NotNull MessageStorable storage, @NotNull ChatRoomStorable chatRoomDBStorage) {
    this.authorisationService = authorisationService;
    this.historyStorage = storage;
    this.chatRoomDBStorage = chatRoomDBStorage;
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
        return new ResponseMessage(HELP);
      }

      //return this user's info
      if (trimmed.startsWith("/user_info")) {
        return new ResponseMessage(handleUserInfo(trimmed));
      }

      //change authorized user's password
      if (trimmed.startsWith("/user_pass")) {
        return new ResponseMessage(handleUserChangePass(trimmed));
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

      //create chat
      if (trimmed.startsWith("/chat_create")) {
        return new ResponseMessage(handleChatCreate(trimmed));
      }

      //find in chat
      if (trimmed.startsWith("/chat_find")) {
        return new ResponseMessage(handleChatFind(trimmed));
      }

      //chatHistory
      if (trimmed.startsWith("/chat_history")) {
        return new ResponseMessage(handleChatHistory(trimmed));
      }

      //available chats
      if (trimmed.startsWith("/chat_list")) {
        return new ResponseMessage(handleChatList(trimmed));
      }

      //finish sending Messages
      if ("/q".equals(trimmed) || "/quit".equals(trimmed)) {
        return new QuitMessage();
      }
    }

    //This means we received general message
    if (authorizedUser != null) {
      message.setAuthor(authorizedUser.getNickname());
      message.setAuthorId(authorizedUser.getId());
      if (historyStorage != null) {
        try {
          historyStorage.addMessage(message);
        } catch (Exception e) {
          log.log(Level.SEVERE, "Adding message exception: ", e);
        }
      }
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
        //historyStorage = new MessageFileStorage(authorizedUser);
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
      String ret = response.response;
      if (response.user != null) {
        authorizedUser = response.user;
        //historyStorage = new MessageFileStorage(authorizedUser);
        ret = ret.concat("\n").concat(handleUserInfo(null));
      }
      return ret;
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
    int OFFSET = 5; //length of string "/user"
    String newNickname = trimmed.substring(OFFSET).trim();
    if (authorizedUser != null) {
      try {
        authorisationService.userStorage.changeNickname(authorizedUser.getId(), newNickname);
      } catch (Exception e) {
        log.log(Level.SEVERE, "Exception: ", e);
        return "Unable to change nickname";
      }
      authorizedUser.setNickname(newNickname);
      return "Nickname " + trimmed.substring(OFFSET).trim() + " set successfully.";
    } else {
      return UNAUTHORIZED;
    }
  }

  @NotNull
  private String handleHistory(@NotNull String trimmed) {
    int OFFSET = 8; //length of string "/history"
    if (authorizedUser != null) {
      String history;
      try {
        if ("/history".equals(trimmed)) {
          history = historyStorage.getHistory(0, authorizedUser);
        } else {
          history = historyStorage.getHistory(Integer.parseInt(trimmed.substring(OFFSET).trim()), authorizedUser);
        }
      } catch (NumberFormatException ex) {
        log.info("Wrong input parameter caught for \"history\"");
        return "Usage: /history <quantity> or /history";
      } catch (Exception e) {
        log.log(Level.SEVERE, "handleHistory exception: ", e);
        return "Unable to find messages. Please try again later";
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
        return historyStorage.findMessage(regex, authorizedUser);
      } catch (PatternSyntaxException e) {
        log.info("Wrong input parameter caught for \"find\"");
        return "Invalid regular expression";
      } catch (Exception e) {
        log.log(Level.SEVERE, "Exception while handleFind: ", e);
        return "Unable to find message. Please try again later";
      }
    } else {
      return UNAUTHORIZED;
    }
  }

  @NotNull
  private String handleChatCreate(@NotNull String trimmed) {
    if (authorizedUser != null) {
      try {
        int OFFSET = "/chat_create".length();
        String[] userlist = trimmed.substring(OFFSET).trim().split(",");
        Long roomId;
        roomId = chatRoomDBStorage.createChatRoom();
        StringBuilder stringBuilder = new StringBuilder();
        for (String userId : userlist) {
          try {
            User foundUser = authorisationService.userStorage.findUserById(Integer.parseInt(userId));
            if (foundUser != null)
              chatRoomDBStorage.addParticipant(roomId, foundUser.getId());
            else
              stringBuilder.append("User ").append(userId).append(" not found\n");
          } catch (NumberFormatException ex) {
            log.info("Wrong input parameter for chatCreate");
            return "Usage: /chat_create <user_id>, <user_id>, ...";
          } catch (Exception e) {
            log.log(Level.SEVERE, "Exception: ", e);
            return "Unable to create chat. Please try again later";
          }
        }
        chatRoomDBStorage.addParticipant(roomId, authorizedUser.getId());
        stringBuilder.append("Chat ").append(roomId).append(" created successfully.");
        return stringBuilder.toString();
      } catch (Exception e) {
        log.log(Level.SEVERE, "Exception: ", e);
        return "Unable to create chat. Please try again later";
      }
    } else {
      return UNAUTHORIZED;
    }
  }

  @NotNull
  private String handleUserInfo(@Nullable String trimmed) {
    if (authorizedUser != null) {
      if (trimmed == null || "/user_info".equals(trimmed))
        return authorizedUser.toString();
      try {
        int OFFSET = "/user_info".length();
        String num = trimmed.substring(OFFSET).trim();
        User foundUser = authorisationService.userStorage.findUserById(Integer.parseInt(num));
        if (foundUser != null)
          return foundUser.toString();
        else
          return "User not found";
      } catch (NumberFormatException ex) {
        log.info("Wrong input parameter for user_info");
        return "Usage: /user_info <user_id>";
      } catch (Exception e) {
        log.log(Level.SEVERE, "Handle User Info Exception: ", e);
        return "Unable to find info";
      }
    } else {
      return UNAUTHORIZED;
    }
  }

  @NotNull
  private String handleUserChangePass(@NotNull String trimmed) {
    if (authorizedUser != null) {
      int OFFSET = "/user_pass".length();
      String[] passOldAndNew = trimmed.substring(OFFSET).trim().split(" ", 2);
      if (passOldAndNew.length != 2) {
        return "Usage: /user_pass <old password> <new password>";
      }
      try {
        AuthorisationServiceResponse response = authorisationService.authorize(authorizedUser.getLogin(), passOldAndNew[0]);
        if (response.user != null)
          return authorisationService.passwordStorage.changePassword(authorizedUser, passOldAndNew[1]);
        else
          return response.response;
      } catch (Exception e) {
        log.log(Level.SEVERE, "Change Password Exception: ", e);
        return "Unable to change password";
      }
    } else {
      return UNAUTHORIZED;
    }
  }

  @NotNull
  private String handleChatList(@NotNull String trimmed) {
    if (authorizedUser != null) {
      int OFFSET = "/chat_list".length();
      try {
        if ("/chat_list".equals(trimmed)) {
          Set<Long> chats = chatRoomDBStorage.getChatsFromUser(authorizedUser.getId());
          if (chats.size() == 0) {
            return "General chat";
          }
          StringBuilder builder = new StringBuilder();
          chats.stream().forEach(c -> builder.append("\n  ").append(c));
          return builder.append("\n  ").append("General chat").toString();
        } else {
          return "Usage: /chat_list";
        }
      } catch (Exception e) {
        log.log(Level.SEVERE, "Change Password Exception: ", e);
        return "Unable to change password";
      }
    } else {
      return "General chat";
    }
  }

  @NotNull
  private String handleChatHistory(@NotNull String trimmed) {
    int OFFSET = "/chat_history".length();
    if (authorizedUser != null) {
      String[] params = trimmed.substring(OFFSET).trim().split(" ", 2);
      try {
        if ("/chat_history".equals(trimmed)) {
          return "Usage: /chat_history <chat_id> <amount>";
        } else {
          long chat_id = Long.parseLong(params[0]);
          if (!chatRoomDBStorage.isParticipant(chat_id, authorizedUser.getId())) {
            return "You don't belong to this conversation";
          }
          if (params.length == 1) {
            return historyStorage.getHistory(0, null, chat_id);
          } else {
            return historyStorage.getHistory(Integer.parseInt(params[1]), null, chat_id);
          }
        }
      } catch (NumberFormatException ex) {
        log.info("Wrong input parameter caught for \"chat_history\"");
        return "Usage: /chat_history <chat_id> <amount>";
      } catch (Exception e) {
        log.log(Level.SEVERE, "handleChatHistory exception: ", e);
        return "Unable to find messages. Please try again later";
      }
    } else {
      return UNAUTHORIZED;
    }
  }

  @NotNull
  private String handleChatFind(@NotNull String trimmed) {
    if (authorizedUser != null) {
      int OFFSET = "/chat_find".length();
      String[] params = trimmed.substring(OFFSET).trim().split(" ", 2);
      try {
        if ("/chat_find".equals(trimmed) || params.length != 2) {
          return "Usage: /chat_find <chat_id> <regex>";
        }
        long chat_id = Long.parseLong(params[0]);
        if (!chatRoomDBStorage.isParticipant(chat_id, authorizedUser.getId())) {
          return "You don't belong to this conversation";
        }
        return historyStorage.findMessage(params[1], authorizedUser, chat_id);
      } catch (NumberFormatException ex) {
        log.info("Wrong input parameter caught for \"chat_history\"");
        return "Usage: /chat_history <chat_id> <amount>";
      } catch (Exception e) {
        log.log(Level.SEVERE, "handleChatHistory exception: ", e);
        return "Unable to find messages. Please try again later";
      }
    } else {
      return UNAUTHORIZED;
    }
  }

  public void stop() {
//    if (historyStorage != null)
//      historyStorage.close();
  }
}
