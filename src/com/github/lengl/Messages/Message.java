package com.github.lengl.Messages;

import com.sun.istack.internal.NotNull;

import java.sql.Timestamp;

public class Message {
  private final String body;
  private final Timestamp time;

  public Message(@NotNull String body, @NotNull Timestamp time) {
    this.body = body;
    this.time = time;
  }

  @NotNull
  public String getBody() {
    return body;
  }

  @NotNull
  public Timestamp getTime() {
    return time;
  }
}
