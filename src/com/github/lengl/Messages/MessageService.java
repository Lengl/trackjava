package com.github.lengl.Messages;

import com.github.lengl.Authorization.AuthorisationClient;
import com.github.lengl.Authorization.User;
import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MessageService {

  private final Logger log = Logger.getLogger(MessageService.class.getName());
  private final BufferedReader bufferedReader;
  private MessageStorable historyStorage;
  private final AuthorisationClient authorisationClient;
  private User authorizedUser;

  public MessageService(@NotNull User user) throws IOException, NoSuchAlgorithmException {
    bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    historyStorage = new MessageStorage(user);
    authorisationClient = new AuthorisationClient("passwordStore.mystore");
    authorizedUser = user;
  }

  public MessageService() throws IOException, NoSuchAlgorithmException {
    bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    authorisationClient = new AuthorisationClient("passwordStore.mystore");
    historyStorage = null;
    authorizedUser = null;
  }

  //TODO: Delete as useless one.
  public void run() {
    try {
      //message reading loop
      while (react(bufferedReader.readLine()) != null) ;
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    }
  }

  public String react(@NotNull String input) {
    Timestamp sendTime = new Timestamp(new java.util.Date().getTime());
    String trimmed = input.trim();
    if (trimmed.startsWith("/")) {
      //login as smone
      //TODO: Rework authorisation cycle
      if (trimmed.startsWith("/login")) {
        handleLogin(trimmed);
        return null;
      }

      //print help message
      if ("/help".equals(trimmed)) {
        return "/help\n" +
            "/login <your login>\n" +
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
      //TODO: define what to do with this one
      if ("/quit".equals(trimmed)) {
        return null;
      }
    }
    if (historyStorage != null) {
      historyStorage.addMessage(new Message(input, sendTime));
    }
    //This null should mean everything is correct and no answer is needed.
    return null;
  }

  private void handleLogin(@NotNull String trimmed) {
    if ("/login".equals(trimmed)) {
      User authorizedTemp = authorisationClient.startAuthorizationCycle();
      try {
        if (authorizedTemp != null) {
          historyStorage = new MessageStorage(authorizedTemp);
          authorizedUser = authorizedTemp;
        }
      } catch (IOException e) {
        log.log(Level.SEVERE, "IOException: ", e);
        System.err.println("Unable to open history. Please try to authorize again");
      }
    } else {
      String[] parsed = trimmed.split(" ", 2);
      try {
        User authorizedTemp = authorisationClient.authorize(parsed[1]);
        if (authorizedTemp != null) {
          historyStorage = new MessageStorage(authorizedUser);
          authorizedUser = authorizedTemp;
        }
      } catch (IOException e) {
        log.log(Level.SEVERE, "IOException: ", e);
        System.err.println("Usage: /login <your login>");
      }
    }
  }

  private String handleNickname(@NotNull String trimmed) {
    //TODO: Probably check if nickname already used & give it a number (e.g. lengl, lengl2, lengl3...)
    int OFFSET = 5; //length of string "/user"
    if (authorizedUser != null) {
      authorizedUser.setNickname(trimmed.substring(OFFSET).trim());
      return "Username " + trimmed.substring(OFFSET).trim() + " set successfully.";
    } else {
      return "You need to be authorized (/login) to use this command.";
    }
  }

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
      return "You need to be authorized (/login) to use this command.";
    }
  }

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
      return "You need to be authorized (/login) to use this command.";
    }
  }

  public void stop() {
    authorisationClient.stopAuthorizationClient();
    try {
      bufferedReader.close();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    }
    historyStorage.close();
  }
}
