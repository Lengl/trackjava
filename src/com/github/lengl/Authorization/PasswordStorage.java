package com.github.lengl.Authorization;

import com.github.lengl.Users.User;
import com.github.lengl.Users.UserStorable;
import com.github.lengl.Users.UserStorage;
import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PasswordStorage implements PasswordStorable {
  private final String SEPARATOR = ";";
  private final Logger log = Logger.getLogger(PasswordStorage.class.getName());
  private final MessageDigest messageDigest;
  private final Map<User, String> passMap = new HashMap<>();
  private final BufferedWriter storeWriter;

  public PasswordStorage(@NotNull String filename, @NotNull UserStorable userStore) throws IOException, NoSuchAlgorithmException {
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
    messageDigest = MessageDigest.getInstance("SHA1");
  }

  public PasswordStorage(@NotNull String filename) throws IOException, NoSuchAlgorithmException {
    this(filename, new UserStorage());
  }

  public PasswordStorage(@NotNull UserStorable userStore) throws IOException, NoSuchAlgorithmException {
    this("passwordStore.mystore", userStore);
  }

  public PasswordStorage() throws IOException, NoSuchAlgorithmException {
    this("passwordStore.mystore", new UserStorage());
  }

  @NotNull
  public void add(@NotNull User user, @NotNull String pass) throws IOException {
    String login = user.getLogin();
    String encodedPass = encode(pass);
    passMap.put(user, encodedPass);
    storeWriter.write(encodedPass + SEPARATOR + login);
    storeWriter.newLine();
    //storeWriter.flush();
    log.info("User " + login + " added to store");
  }

  @NotNull
  public boolean check(User user, String pass) {
    if (passMap.get(user).equals(encode(pass))) {
      log.fine("User " + user.getLogin() + " password check successful");
      return true;
    } else {
      log.info("User " + user.getLogin() + " password check failed");
      return false;
    }
  }

  @NotNull
  private String encode(String input) {
    byte[] result = messageDigest.digest(input.getBytes());

    //convert the byte to hex format
    StringBuilder hexString = new StringBuilder();
    for (byte resultByte : result) {
      hexString.append(Integer.toHexString(0xFF & resultByte));
    }
    return hexString.toString();
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
