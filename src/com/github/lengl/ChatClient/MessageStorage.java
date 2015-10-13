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
  private final BufferedWriter messageWriter;
  private final User parent;

  public MessageStorage(User user) {
    parent = user;
    messageWriter = new BufferedWriter(new OutputStreamWriter(System.out));
  }

  public User getParent() {
    return parent;
  }

  void addMessage(String message) {
    messageHistory.add(message);
  }

  void printHistory (int size) throws IOException {
    int mySize = size;
    if (size > 0) {
      if (size == 0 || size > messageHistory.size())
        mySize = messageHistory.size();
      ListIterator iter = messageHistory.listIterator(messageHistory.size() - mySize);
      while (iter.hasNext()) {
        messageWriter.write((String)iter.next());
        messageWriter.newLine();
      }
      messageWriter.flush();
    }
  }

  void closeStorage() {
    try {
      messageWriter.close();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    }
  }
}
