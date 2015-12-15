package com.github.lengl.Authorization;

import com.github.lengl.Users.User;
import com.github.lengl.Users.UserFileStorage;
import com.github.lengl.Users.UserStorable;
import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PasswordFileStorage implements PasswordStorable {
  private final String SEPARATOR = ";";
  private final Logger log = Logger.getLogger(PasswordFileStorage.class.getName());
  private final Map<User, String> passMap = new HashMap<>();
  private final BufferedWriter storeWriter;

  public PasswordFileStorage(@NotNull String filename, @NotNull UserStorable userStore) throws Exception {
    Path path = FileSystems.getDefault().getPath(filename);
    if (Files.notExists(path)) {
      Files.createFile(path);
      log.info("Empty store created");
    }

    BufferedReader fr = Files.newBufferedReader(path);
    String text;

    while ((text = fr.readLine()) != null) {
      String[] parse = text.split(SEPARATOR, 2);
      //first part is user password hash, other is it's login
      passMap.put(userStore.findUserByLogin(parse[1]), parse[0]);
    }
    fr.close();

    storeWriter = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
  }

  public PasswordFileStorage(@NotNull String filename) throws Exception {
    this(filename, new UserFileStorage());
  }

  public PasswordFileStorage(@NotNull UserStorable userStore) throws Exception {
    this("passwordStore.mystore", userStore);
  }

  public PasswordFileStorage() throws Exception {
    this("passwordStore.mystore", new UserFileStorage());
  }

  @NotNull
  public void add(@NotNull User user, @NotNull String pass) throws IOException {
    String login = user.getLogin();
    String encodedPass = Encoder.encode(pass);
    passMap.put(user, encodedPass);
    storeWriter.write(encodedPass + SEPARATOR + login);
    storeWriter.newLine();
    storeWriter.flush();
    log.info("User " + login + " added to store");
  }

  @NotNull
  public boolean check(User user, String pass) {
    if (passMap.get(user).equals(Encoder.encode(pass))) {
      log.fine("User " + user.getLogin() + " password check successful");
      return true;
    } else {
      log.info("User " + user.getLogin() + " password check failed");
      return false;
    }
  }

  @Override
  public String changePassword(@NotNull User user, @NotNull String pass) throws Exception {
    return "OK";
  }

  public void close() {
    try {
      //there is no check if storeWriter != null because constructor throws exception
      //if they were not created
      storeWriter.close();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    }
  }
}
