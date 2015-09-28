package com.github.lengl.Authorization;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PasswordStore {
  private Map<User, String> passMap = new HashMap<User, String>();
  private Map<String, User> userMap = new HashMap<String, User>();
  private BufferedWriter dbWriter;
  private static final String SEPARATOR = ";";
  private static Logger log = Logger.getLogger(PasswordStore.class.getName());

  public PasswordStore(String filename) throws IOException {
    File file = new File(filename);
    if(!file.exists()) {
      file.createNewFile();
      log.info("Empty database created");
    }

    BufferedReader fr = new BufferedReader(new FileReader(filename));
    String text;

    while ((text = fr.readLine()) != null) {
      String[] parse = text.split(SEPARATOR, 2);
      //first part is user password, other is it's name
      User user = new User(parse[1]);
      userMap.put(parse[1], user);
      passMap.put(user, parse[0]);
    }

    dbWriter = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), true));
  }

  public void addPassword(String name, String pass) throws IOException, NoSuchAlgorithmException {
    User user = new User(name);
    String encodedPass = encode(pass);
    passMap.put(user, encodedPass);
    userMap.put(name, user);
    dbWriter.write(encodedPass + SEPARATOR + name);
    dbWriter.newLine();
    dbWriter.flush();
    log.info("User " + name + " added to database");
  }

  public boolean checkPassword(User user, String pass) throws NoSuchAlgorithmException {
    if (passMap.get(user).equals(encode(pass))) {
      log.fine("User " + user.getName() + " password check successful");
      return true;
    } else {
      log.info("User " + user.getName() + " password check failed");
      return false;
    }
  }

  private static String encode(String input) throws NoSuchAlgorithmException {
    MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
    byte[] result = messageDigest.digest(input.getBytes());

    //convert the byte to hex format
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < result.length; i++) {
      hexString.append(Integer.toHexString(0xFF & result[i]));
    }
    return hexString.toString();
  }

  @Nullable
  public User findUserByName(@NotNull String name) {
    return userMap.get(name);
  }

  @Override
  protected void finalize() throws Throwable {
    dbWriter.close();
    super.finalize();
  }
}
