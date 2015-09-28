package com.github.lengl.Authorization;

import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthorisationClient {
  private final PasswordStore passwordStore;
  private final BufferedReader reader;
  private static Logger log = Logger.getLogger(AuthorisationClient.class.getName());

  public AuthorisationClient(String passwordDatabasePath) throws IOException, NoSuchAlgorithmException {
    passwordStore = new PasswordStore(passwordDatabasePath);
    reader = new BufferedReader(new InputStreamReader(System.in));
  }

  public void startAuthorizationCycle() {
    log.info("Auth cycle started");
    while (true) {
      try {
        System.out.println("Do you want to create new user profile?");
        if (answerIsYes()) {
          while (true) {
            System.out.println("Type your login:");
            String name = reader.readLine();
            if (passwordStore.findUserByName(name) == null) {
              getPasswordAndCreateUser(name);
              break;
            } else {
              System.out.println("This user already exist. Try again.");
            }
          }
        } else {
          authorize();
          System.out.println("Exit the program? (type \"y\" or \"n\")");
          if (answerIsYes()) {
            break;
          }
        }
      } catch (IOException ex) {
        log.log(Level.SEVERE, "IOException: ", ex);
        return;
      }
    }
    log.info("Auth cycle ended");
  }

  private boolean answerIsYes() throws IOException {
    return reader.readLine().toLowerCase().equals("y");
  }

  private boolean tryToGetYesOrNoAnswer() throws IOException {
    String answer;
    while (true) {
      answer = reader.readLine();
      switch (answer.toLowerCase()) {
        case "y":
          return true;
        case "n":
          return false;
        default:
          System.out.println("Couldn't recognize the answer. Please type \"y\" or \"n\"");
          break;
      }
    }
  }

  private void getPasswordAndCreateUser (@NotNull String name) throws IOException {
    while (true) {
      System.out.println("Print your password:");
      String password = safePassRead();
      System.out.println("Confirm your password:");
      String passwordRetyped = safePassRead();
      if (password.equals(passwordRetyped)) {
        passwordStore.addPassword(name, password);
        System.out.println("User created successfully. Now you can authorize with your login and password.");
        log.info("User " + name + " created successfully");
        break;
      } else {
        System.out.println("Passwords didn't match! Try again.");
      }
    }
  }

  private void authorize() throws IOException {
    System.out.println("Authorization started.");
    System.out.println("Type your login:");
    String name = reader.readLine();

    User user = passwordStore.findUserByName(name);
    if (user != null) {
      for (int i = 3; i > 0; i--) {
        System.out.println("Type your password:");
        String pass = safePassRead();
        if (passwordStore.checkPassword(user, pass)) {
          System.out.println("Authorized successfully");
          log.fine("User " + name + "authorized successfully");
          break;
        } else {
          System.out.println("Password incorrect. " + (i - 1) + " attempts left");
          if (i == 1)
            log.info("User " + name + " run out of attempts");
        }
      }
    } else {
      System.out.println("There is no user with this name. Would you like to create one? (type \"y\" or \"n\")");
      if (answerIsYes()) {
        getPasswordAndCreateUser(name);
      }
    }
  }

  private String safePassRead() throws IOException{
    if (System.console() != null) {
      return new String(System.console().readPassword());
    } else {
      return reader.readLine();
    }
  }

  public void stopAuthorizationClient() throws IOException {
    passwordStore.stopPasswordStore();
    reader.close();
  }
}