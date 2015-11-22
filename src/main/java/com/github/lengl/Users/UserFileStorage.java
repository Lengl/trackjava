package com.github.lengl.Users;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserFileStorage implements UserStorable {
  private final static String SEPARATOR = ";";
  private final Logger log = Logger.getLogger(UserFileStorage.class.getName());
  private final Map<String, User> loginUserMap = new HashMap<>();
  private final Map<Long, User> idUserMap = new HashMap<>();
  private final BufferedWriter storeWriter;
  private long lastId = 1;

  public UserFileStorage(@NotNull String filename) throws IOException {
    Path path = FileSystems.getDefault().getPath(filename);
    if (Files.notExists(path)) {
      Files.createFile(path);
      log.info("Empty store created");
    }

    BufferedReader fr = Files.newBufferedReader(path);
    String text;

    while ((text = fr.readLine()) != null) {
      String[] parse = text.split(SEPARATOR, 2);
      //first part is user id, other is it's login
      long id = Long.parseLong(parse[0]);
      if (id > lastId)
        lastId = id;
      User user = new User(parse[1], id);
      loginUserMap.put(parse[1], user);
      idUserMap.put(id, user);
    }

    fr.close();

    storeWriter = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
  }

  public UserFileStorage() throws IOException {
    this("userStore.mystore");
  }

  @Override
  @NotNull
  public User create(@NotNull String login) throws IOException {
    long id = ++lastId;
    User user = new User(login, id);
    loginUserMap.put(login, user);
    idUserMap.put(id, user);
    storeWriter.write(id + SEPARATOR + login);
    storeWriter.newLine();
    storeWriter.flush();
    return user;
  }

  @Override
  @Nullable
  public User findUserByLogin(@NotNull String login) {
    return loginUserMap.get(login);
  }

  @Override
  public User findUserById(@NotNull long id) {
    return idUserMap.get(id);
  }

  @Override
  public String changeNickname(@NotNull Long id, @NotNull String newNickname) throws Exception {
    return "OK";
  }

  @Override
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
