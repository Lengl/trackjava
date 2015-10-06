package com.github.lengl.Authorization;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

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

public class PasswordStore {
  private final String SEPARATOR = ";";
  private final Logger log = Logger.getLogger(PasswordStore.class.getName());
  private final MessageDigest messageDigest;
  private final Map<User, String> passMap = new HashMap<User, String>();
  private final Map<String, User> userMap = new HashMap<String, User>();
  private final BufferedWriter storeWriter;

  public PasswordStore(String filename) throws IOException, NoSuchAlgorithmException {
    Path path = FileSystems.getDefault().getPath(filename);
    if (Files.notExists(path)) {
      Files.createFile(path);
      log.info("Empty store created");
    }

    BufferedReader fr = Files.newBufferedReader(path);
    String text;

    while ((text = fr.readLine()) != null) {
      String[] parse = text.split(SEPARATOR, 2);
      //first part is user password, other is it's name
      User user = new User(parse[1]);
      userMap.put(parse[1], user);
      passMap.put(user, parse[0]);
    }
    fr.close();

    storeWriter = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
    messageDigest = MessageDigest.getInstance("SHA1");
  }

  @NotNull
  public void addPassword(@NotNull String name, @NotNull String pass) throws IOException {
    User user = new User(name);
    String encodedPass = encode(pass);
    passMap.put(user, encodedPass);
    userMap.put(name, user);
    storeWriter.write(encodedPass + SEPARATOR + name);
    storeWriter.newLine();
    //storeWriter.flush();
    log.info("User " + name + " added to store");
  }

  @NotNull
  public boolean checkPassword(User user, String pass) {
    if (passMap.get(user).equals(encode(pass))) {
      log.fine("User " + user.getName() + " password check successful");
      return true;
    } else {
      log.info("User " + user.getName() + " password check failed");
      return false;
    }
  }

  @NotNull
  private String encode(String input) {
    byte[] result = messageDigest.digest(input.getBytes());

    //convert the byte to hex format
    StringBuffer hexString = new StringBuffer();
    for (byte resultByte : result) {
      hexString.append(Integer.toHexString(0xFF & resultByte));
    }
    return hexString.toString();
  }

  @Nullable
  public User findUserByName(@NotNull String name) {
    return userMap.get(name);
  }

  public void closePasswordStore() {
    try {
      //there is no check if storeWriter != null because constructor throws exception
      //if they were not created
      storeWriter.close();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    }
  }
}
