package com.github.lengl.ChatClient;

public interface MessageStorable {

  void addMessage(Message message);

  String findMessage(String regex);

  String getHistory(int size);

  void close();
}
