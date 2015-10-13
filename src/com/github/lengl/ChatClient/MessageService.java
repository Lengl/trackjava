package com.github.lengl.ChatClient;

import com.github.lengl.Authorization.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageService {

  private final Logger log = Logger.getLogger(MessageService.class.getName());
  private final BufferedReader bufferedReader;
  private final MessageStorage historyStorage;

  private String nickname;

  public MessageService(User user) {
    bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    historyStorage = new MessageStorage(user);
  }

  public void run() {
    log.info("User " + historyStorage.getParent().getName() + " started chat session");
    try {
      while (react(bufferedReader.readLine()));
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    }
    log.info("User " + historyStorage.getParent().getName() + " ended chat session");
  }

  private boolean react(String input) {
    historyStorage.addMessage(input);
    input.trim();
    if(input.startsWith("\\")) {
      //print help message
      if ("\\help".equals(input)) {
        System.out.println("\\help\n\\user <nickname>\n\\history <amount>\n\\quit");
      }

      if (input.startsWith("\\user ")) {
        //TODO: There should probably be a better way then this one. I need some ideas
        nickname = input.substring(6).trim();
      }

      if (input.startsWith("\\history")) {
        try {
          //TODO: There should probably be a better way then this one. I need some ideas
          if(input.equals("\\history")) {
            historyStorage.printHistory(0);
          } else {
            historyStorage.printHistory(Integer.parseInt(input.substring(9).trim()));
          }
        } catch (IOException e) {
          log.log(Level.SEVERE, "IOException: ", e);
          System.out.println("Usage: \\history <quantity> or \\history");
        }
      }

      if ("\\quit".equals(input)) {
        return false;
      }
    }
    return true;
  }

  public void stopMessageService() {
    try {
      bufferedReader.close();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    }
    historyStorage.closeStorage();
  }
}
