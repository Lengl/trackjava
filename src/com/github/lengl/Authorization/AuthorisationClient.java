package com.github.lengl.Authorization;

import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AuthorisationClient {
  private final PasswordStore pStore;
  private final BufferedReader reader;

  public AuthorisationClient(/*String passwordDatabasePath*/) throws IOException {
    pStore = new PasswordStore(/*passwordDatabasePath*/);
    reader = new BufferedReader(new InputStreamReader(System.in));
  }

  public void startAuthorizationCycle() {
    while (true) {
      try {
        System.out.println("Authorization started.");
        System.out.println("Type your login:");
        String name = reader.readLine();

        User user = pStore.findUserByName(name);
        if (user != null) {
          //3 attempts to type correct password
          for (int i = 0; i < 3; i++) {
            System.out.println("Type your password:");
            String pass = reader.readLine();
            if (pStore.checkPassword(user, pass)) {
              System.out.println("Authorized successfully");
              break;
            } else {
              System.out.println("Password incorrect. " + (2 - i) + " attempts left");
            }
          }
        } else {
          System.out.println("There is no user with this name. Would you like to create one? (type \"y\" or \"n\")");
          if (answerIsYes()) {
            getPasswordAndCreateUser(name);
          }
        }
        System.out.println("Exit the program? (type \"y\" or \"n\")");
        if (answerIsYes()) {
          break;
        }
      } catch (IOException ex) {
        System.err.println("A trouble with I/O system occurred: " + ex.getMessage());
        System.err.println("Please restart application");
        return;
      }
    }
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

  private void getPasswordAndCreateUser (@NotNull String name) throws IOException {
    while (true) {
      System.out.println("Print your password:");
      String password = reader.readLine();
      System.out.println("Confirm your password:");
      String passwordRetyped = reader.readLine();
      if (password.equals(passwordRetyped)) {
        pStore.addPassword(name, password);
        System.out.println("User created successfully. Now you can authorize with your login and password.");
        break;
      } else {
        System.out.println("Passwords didn't match! Try again.");
      }
    }
  }
}