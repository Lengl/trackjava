package com.github.lengl.Authorization;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PasswordStore {
  private Map<User, String> passMap = new HashMap<User, String>();
  private Map<String, User> userMap = new HashMap<String, User>();
  private BufferedWriter dbWriter;

  public PasswordStore(/*String filename*/) throws IOException {
    /*BufferedReader fr = new BufferedReader(new FileReader(filename));
    String text = null;

    while ((text = fr.readLine()) != null) {
      //result = parseText
      //userList.put(result);
    }

    dbWriter = new BufferedWriter(new FileWriter(filename, true));*/
  }

  public void addPassword(String name, String pass) throws IOException {
    User user = new User(name);
    passMap.put(user, pass);
    userMap.put(name, user);
    //dbWriter.write(pass);
    //log it
  }


  @Nullable
  public User findUserByName(@NotNull String name) {
    return userMap.get(name);
  }


  public boolean checkPassword(User user, String pass) {
    if (passMap.get(user).equals(pass)) {
      //log it
      return true;
    } else {
      //log it
      return false;
    }
  }
}
