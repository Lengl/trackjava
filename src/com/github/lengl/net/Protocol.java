package com.github.lengl.net;

import com.github.lengl.Messages.Message;

import java.sql.Timestamp;

public class Protocol {

  public static Message decode(byte[] bytes) {
    String data = new String(bytes);
    Timestamp time = new Timestamp(new java.util.Date().getTime());
    return new Message(data, time);
  }

  public static byte[] encode(Message msg) {
    return msg.getBody().getBytes();
  }

}