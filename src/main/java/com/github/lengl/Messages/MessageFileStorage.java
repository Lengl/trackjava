package com.github.lengl.Messages;

import com.github.lengl.Users.User;
import com.sun.istack.internal.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageFileStorage implements MessageStorable {

  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
  private final String SEPARATOR = ";";
  private final String STOREFOLDER = "messages/";
  private final BufferedWriter storeWriter;
  private final Logger log = Logger.getLogger(MessageFileStorage.class.getName());
  private final List<Message> messageHistory = new ArrayList<>();

  public MessageFileStorage(@NotNull User user) throws IOException {
    Path path = FileSystems.getDefault().getPath(STOREFOLDER + user.getLogin() + ".mystore");
    if (Files.notExists(path)) {
      Files.createFile(path);
      log.info("Empty store created");
    }

    BufferedReader fr = Files.newBufferedReader(path);
    String text;

    while ((text = fr.readLine()) != null) {
      String[] parse = text.split(SEPARATOR, 2);
      //first part is timestamp, the rest is body
      try {
        Date sendDate = dateFormat.parse(parse[0]);
        messageHistory.add(new Message(parse[1], new Timestamp(sendDate.getTime())));
      } catch (ParseException e) {
        log.log(Level.SEVERE, "ParseException: ", e);
      }
    }
    fr.close();

    storeWriter = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
  }

  public MessageFileStorage(long chatId) throws IOException {
    Path path = FileSystems.getDefault().getPath(STOREFOLDER + "chat" + chatId + ".mystore");
    if (Files.notExists(path)) {
      Files.createFile(path);
      log.info("Empty store created");
    }

    BufferedReader fr = Files.newBufferedReader(path);
    String text;

    while ((text = fr.readLine()) != null) {
      String[] parse = text.split(SEPARATOR, 2);
      //first part is timestamp, the rest is body
      try {
        Date sendDate = dateFormat.parse(parse[0]);
        messageHistory.add(new Message(parse[1], new Timestamp(sendDate.getTime())));
      } catch (ParseException e) {
        log.log(Level.SEVERE, "ParseException: ", e);
      }
    }
    fr.close();

    storeWriter = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
  }

  public void addMessage(@NotNull Message message) {
    messageHistory.add(message);
    try {
      storeWriter.write(message.getTime().toString() + SEPARATOR + message.getBody());
      storeWriter.newLine();
      storeWriter.flush();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IO Exception: ", e);
    }
  }

  @NotNull
  public String getHistory(int size, User user) {
    int mySize = size;
    if (size <= 0 || size > messageHistory.size())
      mySize = messageHistory.size();
    ListIterator<Message> msgHistoryIterator = messageHistory.listIterator(messageHistory.size() - mySize);
    StringBuilder buffer = new StringBuilder();
    while (msgHistoryIterator.hasNext()) {
      buffer.append("\n  ").append(msgHistoryIterator.next().getBody());
    }
    String ret = buffer.toString();
    if (ret.isEmpty()) {
      return "empty";
    } else {
      return ret;
    }
  }

  @Override
  public String getHistory(int size, User user, Long chat_id) {
    return null;
  }

  @NotNull
  public String findMessage(@NotNull String regex, User user) {
    ListIterator<Message> msgHistoryIterator = messageHistory.listIterator(0);
    StringBuilder buffer = new StringBuilder();
    while (msgHistoryIterator.hasNext()) {
      String tmp = msgHistoryIterator.next().getBody();
      if (tmp.matches(regex)) {
        buffer.append(tmp);
        buffer.append("\n");
      }
    }
    if (buffer.length() == 0) {
      return "No matches";
    } else {
      return buffer.toString();
    }
  }

  @Override
  public String findMessage(String regex, User user, Long chat_id) throws Exception {
    return null;
  }

  public void close() {
    try {
      storeWriter.close();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IO Exception: ", e);
    }
  }
}
