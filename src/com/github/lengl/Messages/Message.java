package com.github.lengl.Messages;

import java.sql.Timestamp;

public class Message {
  private final String body;
  private final Timestamp time;

  public Message(String body, Timestamp time) {
    this.body = body;
    this.time = time;
  }

  public String getBody() {
    return body;
  }

  public Timestamp getTime() {
    return time;
  }
}
