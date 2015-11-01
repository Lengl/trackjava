package com.github.lengl.Messages;

import com.sun.istack.internal.NotNull;

import java.sql.Timestamp;

public class Message {
  private long id;
  private long senderId;
  private long chatId;
  private final String body;
  private final Timestamp time;

  public Message(@NotNull String body, @NotNull Timestamp time) {
    this.body = body;
    this.time = time;
  }

  public Message(@NotNull String body) {
    this.body = body;
    this.time = new Timestamp(new java.util.Date().getTime());
  }

  @NotNull
  public String getBody() {
    return body;
  }

  @NotNull
  public Timestamp getTime() {
    return time;
  }

  @Override
  public String toString() {
    return "Message{" +
        "message=\'" + body + "\'" +
        ", sender=" + senderId +
        "}";
  }
}
