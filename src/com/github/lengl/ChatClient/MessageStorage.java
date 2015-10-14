package com.github.lengl.ChatClient;

import com.github.lengl.Authorization.User;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageStorage {

  private final List<String> messageHistory = new ArrayList<>();
  private final Logger log = Logger.getLogger(MessageStorage.class.getName());
  private final User owner;

  public MessageStorage(User user) {
    owner = user;
  }

  public User getOwner() {
    return owner;
  }

  public void addMessage(String message) {
    messageHistory.add(message);
  }

  public void printHistory(int size) throws IOException {
    int mySize = size;
    if (size <= 0 || size > messageHistory.size())
      mySize = messageHistory.size();
    ListIterator<String> msgHistoryIterator = messageHistory.listIterator(messageHistory.size() - mySize);
    StringBuilder buffer = new StringBuilder();
    while (msgHistoryIterator.hasNext()) {
      buffer.append(msgHistoryIterator.next());
      buffer.append("\n");
    }
    System.out.println(buffer.toString());
  }

  public void findInHistory(String regex) throws IOException {
    ListIterator<String> msgHistoryIterator = messageHistory.listIterator(0);
    StringBuilder buffer = new StringBuilder();
    while (msgHistoryIterator.hasNext()) {
      String tmp = msgHistoryIterator.next();
      if (tmp.matches(regex)) {
        buffer.append(tmp);
        buffer.append("\n");
      }
    }
    if (buffer.length() == 0) {
      System.out.println("No matches");
    } else {
      System.out.println(buffer.toString());
    }
  }
}
