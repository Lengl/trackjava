package com.github.lengl.ChatClient;

import com.github.lengl.Authorization.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MessageService {

  private final Logger log = Logger.getLogger(MessageService.class.getName());
  private final BufferedReader bufferedReader;
  private final MessageStorable historyStorage;
  private final User authorizedUser;

  public MessageService(User user) throws IOException {
    bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    historyStorage = new MessageStorage(user);
    authorizedUser = user;
  }

  public void run() {
    log.info("User " + authorizedUser.getLogin() + " started chat session");
    try {
      //message reading loop
      while (react(bufferedReader.readLine())) ;
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    }
    log.info("User " + authorizedUser.getLogin() + " ended chat session");
  }

  private boolean react(String input) {
    Timestamp sendTime = new Timestamp(new java.util.Date().getTime());
    String trimmed = input.trim();
    if (trimmed.startsWith("\\")) {
      //print help message
      if ("\\help".equals(trimmed)) {
        System.out.println("\\help\n\\user <nickname>\n\\history <amount>\n\\quit");
        return true;
      }

      //change user nickname
      if (trimmed.startsWith("\\user")) {
        //TODO: There should probably be a better way then this one. I need some ideas
        //TODO: Should I check for empty nickname?.
        //TODO: Probably check if nickname already used & give it a number (e.g. lengl, lengl2, lengl3...)
        authorizedUser.setNickname(trimmed.substring(5).trim());
        return true;
      }

      //print user's message history
      if (trimmed.startsWith("\\history")) {
        //TODO: There should probably be a better way then this one. I need some ideas
        String history = "empty";
        if ("\\history".equals(trimmed)) {
          history = historyStorage.getHistory(0);
        } else {
          try {
            history = historyStorage.getHistory(Integer.parseInt(trimmed.substring(8).trim()));
          } catch (NumberFormatException ex) {
            log.info("Wrong input parameter caught for \"history\"");
            System.out.println("Usage: \\history <quantity> or \\history");
          }
        }
        System.out.println(history);
        return true;
      }

      //find messages matching regex
      if (trimmed.startsWith("\\find")) {
        try {
          String regex = trimmed.substring(5).trim();
          Pattern.compile(regex);
          System.out.println(historyStorage.findMessage(regex));
        } catch (PatternSyntaxException e) {
          log.info("Wrong input parameter caught for \"find\"");
          System.out.println("Invalid regular expression");
        }
        return true;
      }

      //finish sending messages
      if ("\\quit".equals(trimmed)) {
        return false;
      }
    }
    historyStorage.addMessage(new Message(input, sendTime));
    return true;
  }

  public void stop() {
    try {
      bufferedReader.close();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    }
    historyStorage.close();
  }
}
