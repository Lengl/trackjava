package com.github.lengl.ChatClient;

import com.github.lengl.Authorization.User;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

public class MessageStorage implements MessageStorable {

  private final List<Message> messageHistory = new ArrayList<>();
  private final Logger log = Logger.getLogger(MessageStorage.class.getName());
  private final User owner;

  public MessageStorage(User user) {
    owner = user;
  }

  public User getOwner() {
    return owner;
  }

  public void addMessage(Message message) {
    messageHistory.add(message);
  }

  public String getHistory(int size) {
    int mySize = size;
    if (size <= 0 || size > messageHistory.size())
      mySize = messageHistory.size();
    ListIterator<Message> msgHistoryIterator = messageHistory.listIterator(messageHistory.size() - mySize);
    StringBuilder buffer = new StringBuilder();
    while (msgHistoryIterator.hasNext()) {
      buffer.append(msgHistoryIterator.next().getBody());
      buffer.append("\n");
    }
    return buffer.toString();
  }

  public String findMessage(String regex) {
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
}
