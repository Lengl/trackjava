package com.github.lengl.net;

import com.github.lengl.Messages.Message;

import java.io.IOException;

public interface ConnectionHandler extends Runnable {

  void send(Message message) throws IOException;

  void addListener(MessageListener listener);

  void stop();
}
