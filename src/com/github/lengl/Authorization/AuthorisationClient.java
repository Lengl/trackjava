package com.github.lengl.Authorization;

import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthorisationClient {
  private final PasswordStore pStore;
  private final BufferedReader reader;
  private static Logger log = Logger.getLogger(AuthorisationClient.class.getName());

  public AuthorisationClient(String passwordDatabasePath) throws IOException {
    pStore = new PasswordStore(passwordDatabasePath);
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
            if (pStore.findUserByName(name) == null) {
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
      } catch (NoSuchAlgorithmException ex) {
        log.log(Level.SEVERE, "NoSuchAlgorithmException: ", ex);
      }
    }
    log.info("Auth cycle ended");
  }

  private boolean answerIsYes() throws IOException {
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

  private void getPasswordAndCreateUser (@NotNull String name) throws IOException, NoSuchAlgorithmException {
    while (true) {
      System.out.println("Print your password:");
      String password = safePassRead();
      System.out.println("Confirm your password:");
      String passwordRetyped = safePassRead();
      if (password.equals(passwordRetyped)) {
        pStore.addPassword(name, password);
        System.out.println("User created successfully. Now you can authorize with your login and password.");
        log.info("User " + name + " created successfully");
        break;
      } else {
        System.out.println("Passwords didn't match! Try again.");
      }
    }
  }

  private void authorize() throws IOException, NoSuchAlgorithmException {
    System.out.println("Authorization started.");
    System.out.println("Type your login:");
    String name = reader.readLine();

    User user = pStore.findUserByName(name);
    if (user != null) {
      for (int i = 3; i > 0; i--) {
        System.out.println("Type your password:");
        String pass = safePassRead();
        if (pStore.checkPassword(user, pass)) {
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
}