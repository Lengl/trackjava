package com.github.lengl.Authorization;

import com.github.lengl.Users.User;
import com.github.lengl.Users.UserFileStorage;
import com.github.lengl.Users.UserStorable;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthorisationService {
  private final Logger log = Logger.getLogger(AuthorisationService.class.getName());
  public final UserStorable userStorage;
  private PasswordStorable passwordStorage;

  public AuthorisationService() throws IOException, NoSuchAlgorithmException {
    this.userStorage = new UserFileStorage();
    this.passwordStorage = new PasswordFileStorage(userStorage);
  }

  public AuthorisationService(UserStorable userStorage, PasswordStorable passwordStorage) {
    this.userStorage = userStorage;
    this.passwordStorage = passwordStorage;
  }

  //ask user to type his password twice, compare them, create user and add his password to store
  @NotNull
  public AuthorisationServiceResponse createNewUserAndAuthorise(@NotNull String login, @NotNull String password) {
    if (userStorage.findUserByLogin(login) != null) {
      return new AuthorisationServiceResponse(null, "User already exists. Try another login.");
    } else {
      try {
        User authorizedUser = userStorage.create(login);
        passwordStorage.add(authorizedUser, password);
        return new AuthorisationServiceResponse(authorizedUser, "User registered succesfully!");
      } catch (IOException e) {
        log.log(Level.SEVERE, "handleSignin IO Exception:", e);
        return new AuthorisationServiceResponse(null, "Server error. Usage: /signin <your login> <your password>");
      }
    }
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

  //free our resources
  public void stop() {
    userStorage.close();
    passwordStorage.close();
  }
}