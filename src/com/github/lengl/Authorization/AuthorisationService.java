package com.github.lengl.Authorization;

import com.github.lengl.Users.User;
import com.github.lengl.Users.UserStorable;
import com.github.lengl.Users.UserStorage;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthorisationService {
  private final Logger log = Logger.getLogger(AuthorisationService.class.getName());
  private final PasswordStorable passwordStorage;
  private final UserStorable userStorage;
  private final BufferedReader reader;


  public AuthorisationService(@NotNull String passwordStoragePath, @NotNull String userStoragePath) throws IOException, NoSuchAlgorithmException {
    userStorage = new UserStorage(userStoragePath);
    passwordStorage = new PasswordStorage(passwordStoragePath, userStorage);
    reader = new BufferedReader(new InputStreamReader(System.in));
  }

  public AuthorisationService() throws IOException, NoSuchAlgorithmException {
    userStorage = new UserStorage();
    passwordStorage = new PasswordStorage(userStorage);
    reader = new BufferedReader(new InputStreamReader(System.in));
  }

  @Nullable
  public User startAuthorizationCycle() {
    log.info("Auth cycle started");
    //infinite loop1
    while (true) {
      try {
        System.out.println("Do you want to create new user profile?");
        if (answerIsYes()) {
          //infinite loop2
          while (true) {
            System.out.println("Type your login:");
            String login = reader.readLine();
            //loop2 break condition
            if (userStorage.findUserByLogin(login) == null) {
              return createNewUserAndAuthorise(login);
            } else {
              System.out.println("This user already exist. Try again.");
            }
          }
        } else {
          User user = authorize();
          if (user != null) {
            //loop1 break condition
            log.info("Auth cycle ended");
            return user;
          }
          //TODO: Consider if there should be a way to quit without authorization
        }
      } catch (IOException ex) {
        log.log(Level.SEVERE, "IOException: ", ex);
        return null;
      }
    }
  }

  //returns true if user typed "y" or "yes" and false otherwise
  @NotNull
  private boolean answerIsYes() throws IOException {
    String answer = reader.readLine().toLowerCase();
    return "y".equals(answer) || "yes".equals(answer);
  }

  @NotNull
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

  //ask user to type his password twice, compare them, create user and add his password to store
  @NotNull
  private User createNewUserAndAuthorise(@NotNull String login) throws IOException {
    //infinite loop
    while (true) {
      System.out.println("Print your password:");
      String password = safePassRead();
      System.out.println("Confirm your password:");
      String passwordRetyped = safePassRead();
      //loop break condition
      if (password.equals(passwordRetyped)) {
        User user = userStorage.create(login);
        passwordStorage.add(user, password);
        System.out.println("User created successfully. Welcome!");
        log.info("User " + login + " created successfully");
        return user;
      } else {
        System.out.println("Passwords didn't match! Try again.");
      }
    }
  }

  //ask user login, give user 3 attempts to type correct password.
  @Nullable
  public User authorize() throws IOException {
    System.out.println("Authorization started.");
    System.out.println("Type your login:");
    String login = reader.readLine();

    return authorize(login);
  }

  @Nullable
  public User authorize(@NotNull String login) throws IOException {
    User user = userStorage.findUserByLogin(login);
    if (user != null) {
      for (int i = 3; i > 0; i--) {
        System.out.println("Type your password:");
        String pass = safePassRead();
        if (passwordStorage.check(user, pass)) {
          System.out.println("Authorized successfully");
          log.fine("User " + login + "authorized successfully");
          return user;
        } else {
          System.out.println("Password incorrect. " + (i - 1) + " attempts left");
          if (i == 1) {
            log.info("User " + login + " run out of attempts");
          }
        }
      }
    } else {
      System.out.println("There is no user with this login. Would you like to create one? (type \"y\" or \"n\")");
      if (answerIsYes()) {
        return createNewUserAndAuthorise(login);
      }
      return null;
    }
    return null;
  }

  @Nullable
  public AuthorisationServiceResponse authorize(@NotNull String login, @NotNull String password) {
    User user = userStorage.findUserByLogin(login);
    if (user != null) {
      if (passwordStorage.check(user, password)) {
        log.fine("User " + login + "authorised successfully");
        return new AuthorisationServiceResponse(user, "Authorised successfully");
      } else {
        log.info("Password incorrect.");
        return new AuthorisationServiceResponse(null, "Incorrect password");
      }
    } else {
      log.info("User not found");
      return new AuthorisationServiceResponse(null, "There is no user with this login.");
    }
  }

  //if there is a console - tries to readPassword (without echoing), otherwise read normally
  @NotNull
  private String safePassRead() throws IOException {
    if (System.console() != null) {
      return new String(System.console().readPassword());
    } else {
      return reader.readLine();
    }
  }

  //free our resources
  public void stopAuthorizationClient() {
    //there is no check if passwordStorage != null or reader != null because constructor throws exception
    //if they were not created
    passwordStorage.close();
    userStorage.close();
    try {
      reader.close();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    }
  }
}