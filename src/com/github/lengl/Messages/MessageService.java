package com.github.lengl.Messages;

import com.github.lengl.Authorization.AuthorisationService;
import com.github.lengl.Authorization.AuthorisationServiceResponse;
import com.github.lengl.Users.User;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MessageService implements InputHandler {

  private static final String UNAUTHORIZED =
      "You need to authorise (/login) or to register (/signin) yourself to use this command.";
  private final Logger log = Logger.getLogger(MessageService.class.getName());
  private final AuthorisationService authorisationService;
  private MessageStorable historyStorage = null;
  private User authorizedUser = null;

  public MessageService(@NotNull AuthorisationService authorisationService) {
    this.authorisationService = authorisationService;
  }

  public MessageService() throws IOException, NoSuchAlgorithmException {
    this(new AuthorisationService());
  }

  @Nullable
  public String react(@NotNull String input) {
    Timestamp sendTime = new Timestamp(new java.util.Date().getTime());
    String trimmed = input.trim();
    if (trimmed.startsWith("/")) {
      //login as smone
      //TODO: Rework authorisation cycle
      if (trimmed.startsWith("/login")) {
        return handleLogin(trimmed);
      }

      //sign in
      if (trimmed.startsWith("/signin")) {
        return handleSignin(trimmed);
      }

      //print help message
      if ("/help".equals(trimmed)) {
        return "/help\n" +
            "/login <login> <password>\n" +
            "/signin <login> <password>" +
            "/user <nickname>\n" +
            "/history <amount>\n" +
            "/quit";
      }

      //change user nickname
      if (trimmed.startsWith("/user")) {
        return handleNickname(trimmed);
      }

      //print user's message history
      if (trimmed.startsWith("/history")) {
        return handleHistory(trimmed);
      }

      //find messages matching regex
      if (trimmed.startsWith("/find")) {
        return handleFind(trimmed);
      }

      //finish sending messages
      if ("/q".equals(trimmed) || "/quit".equals(trimmed)) {
        return "Goodbye!";
      }
    }
    if (historyStorage != null) {
      historyStorage.addMessage(new Message(input, sendTime));
    }
    //This null should mean everything is correct and we should just send message to everyone.
    return null;
  }

  @Override
  public String getAuthor() {
    if (authorizedUser == null)
      return null;
    return authorizedUser.getNickname();
  }

  @NotNull
  private String handleSignin(@NotNull String trimmed) {
    int OFFSET = 7; //length of string /signin
    String userAndPwd = trimmed.substring(OFFSET).trim();
    String[] parsed = userAndPwd.trim().split(" ", 2);
    if (parsed.length == 1) {
      return "Usage: /signin <your login> <your password>";
    }
    AuthorisationServiceResponse response = authorisationService.createNewUserAndAuthorise(parsed[0], parsed[1]);
    if (response.user != null)
      authorizedUser = response.user;
    return response.response;
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
        historyStorage = new MessageStorage(authorizedUser);
      }
      return response.response;
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
      return "Usage: /login <your login> <your password>";
    }
  }

  @NotNull
  private String handleNickname(@NotNull String trimmed) {
    //TODO: Probably check if nickname already used & give it a number (e.g. lengl, lengl2, lengl3...)
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

  public void stop() {
    authorisationService.stop();
    historyStorage.close();
  }
}
