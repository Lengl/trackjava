package com.github.lengl.Messages;

public interface MessageStorable {

  void addMessage(Message message);

  String findMessage(String regex);

  String getHistory(int size);

  void close();
}
