package com.github.lengl.Authorization;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PasswordStore {
  private Map<User, String> passMap = new HashMap<User, String>();
  private Map<String, User> userMap = new HashMap<String, User>();
  private BufferedWriter dbWriter;
  private static final String separator = new String(";");
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
      String[] parse = text.split(separator, 2);
      //first part is user password, other is it's name
      User user = new User(parse[1]);
      userMap.put(parse[1], user);
      passMap.put(user, parse[0]);
    }

    dbWriter = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), true));
  }



  public void addPassword(String name, String pass) throws IOException {
    User user = new User(name);
    passMap.put(user, pass);
    userMap.put(name, user);
    dbWriter.write(pass + separator + name);
    dbWriter.newLine();
    dbWriter.flush();
    log.info("User " + name + " added to database");
  }


  @Nullable
  public User findUserByName(@NotNull String name) {
    return userMap.get(name);
  }


  public boolean checkPassword(User user, String pass) {
    if (passMap.get(user).equals(pass)) {
      log.fine("User " + user.getName() + " password check successful");
      return true;
    } else {
      log.info("User " + user.getName() + " password check failed");
      return false;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    dbWriter.close();
    super.finalize();
  }
}
