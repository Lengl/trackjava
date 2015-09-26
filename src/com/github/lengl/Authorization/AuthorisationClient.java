package com.github.lengl.Authorization;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AuthorisationClient {
  private static final List<User> userList = new ArrayList<>();
  private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

  public static void main(String[] args) {
    while (true) {
      try {
        System.out.println("Authorization started.");
        System.out.println("Type your login:");
        String name = br.readLine();

        User user = userFind(name);
        if (user != null) {
          checkPassword(user);
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

  private static void checkPassword(User user) throws IOException {
    for (int i = 0; i < 3; i++) {
      System.out.println("Type your password:");
      String password = br.readLine();
      if (user.passwordIs(password)) {
        System.out.println("Authorized successfully!");
        return;
      } else {
        System.out.println("Password didn't match! " + (2 - i) + " attempts left.");
      }
    }
  }

  @Nullable private static User userFind(@NotNull String name) {
    for (User user : userList) {
      if (user.nameIs(name)) {
        return user;
      }
    }
    return null;
  }

  private static boolean answerIsYes() throws IOException {
    String answer;
    while (true) {
      answer = br.readLine();
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

  private static void getPasswordAndCreateUser (@NotNull String name) throws IOException {
    while (true) {
      System.out.println("Print your password:");
      String password = br.readLine();
      System.out.println("Confirm your password:");
      String passwordRetyped = br.readLine();
      if (password.equals(passwordRetyped)) {
        userList.add(new User(name, password));
        System.out.println("User created successfully. Now you can authorize with your login and password.");
        break;
      } else {
        System.out.println("Passwords didn't match! Try again.");
      }
    }
  }
}