package com.github.lengl.net;


import com.github.lengl.Messages.Message;

public interface MessageListener {
  void onMessage(Message message);
}
