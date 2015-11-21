package com.github.lengl.Messages;

import com.github.lengl.Users.User;

public interface MessageStorable {

  void addMessage(Message message) throws Exception;

  String findMessage(String regex, User user) throws Exception;

  String findMessage(String regex, User user, Long chat_id) throws Exception;

  String getHistory(int size, User user) throws Exception;

  String getHistory(int size, User user, Long chat_id) throws Exception;

  void close();
}
