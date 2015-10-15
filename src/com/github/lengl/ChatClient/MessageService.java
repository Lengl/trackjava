package com.github.lengl.ChatClient;

import com.github.lengl.Authorization.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MessageService {

  private final Logger log = Logger.getLogger(MessageService.class.getName());
  private final BufferedReader bufferedReader;
  private final MessageStorage historyStorage;

  public MessageService(User user) {
    bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    historyStorage = new MessageStorage(user);
  }

  public void run() {
    log.info("User " + historyStorage.getOwner().getLogin() + " started chat session");
    try {
      //message reading loop
      while (react(bufferedReader.readLine())) ;
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    }
    log.info("User " + historyStorage.getOwner().getLogin() + " ended chat session");
  }

  private boolean react(String input) {
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
        historyStorage.getOwner().setNickname(trimmed.substring(5).trim());
        return true;
      }

      //print user's message history
      if (trimmed.startsWith("\\history")) {
        try {
          //TODO: There should probably be a better way then this one. I need some ideas
          if (trimmed.equals("\\history")) {
            historyStorage.printHistory(0);
          } else {
            try {
              historyStorage.printHistory(Integer.parseInt(trimmed.substring(8).trim()));
            } catch (NumberFormatException ex) {
              log.info("Wrong input parameter caught");
              System.out.println("Usage: \\history <quantity> or \\history");
            }
          }
        } catch (IOException e) {
          log.log(Level.SEVERE, "IOException: ", e);
          System.out.println("Usage: \\history <quantity> or \\history");
        }
        return true;
      }

      //find messages matching regex
      if (trimmed.startsWith("\\find")) {
        try {
          String regex = trimmed.substring(5).trim();
          Pattern.compile(regex);
          historyStorage.findInHistory(regex);
        } catch (PatternSyntaxException e) {
          System.out.println("Invalid regular expression");
        } catch (IOException e) {
          log.log(Level.SEVERE, "IOException: ", e);
          System.out.println("Usage: \\find <regular expression>");
        }
        return true;
      }

      //finish sending messages
      if ("\\quit".equals(trimmed)) {
        return false;
      }
    }
    historyStorage.addMessage(input);
    return true;
  }

  public void stopMessageService() {
    try {
      bufferedReader.close();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    }
  }
}
