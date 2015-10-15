package com.github.lengl.ChatClient;

public interface MessageStorable {

  void addMessage(String message);

  String findMessage(String regex);

  String getHistory(int size);
}
