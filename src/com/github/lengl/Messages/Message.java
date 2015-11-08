package com.github.lengl.Messages;

import com.sun.istack.internal.NotNull;

import java.io.Serializable;
import java.sql.Timestamp;

public class Message implements Serializable {
  private long id;
  private long senderId;
  private long chatId;
  private String author;
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

  @NotNull
  public long getSenderId() {
    return senderId;
  }

  @Override
  public String toString() {
    return "Message{" +
        "message=\'" + body + "\'" +
        ", author=" + author +
        "}";
  }

  public void setSenderId(long senderId) {
    this.senderId = senderId;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
}
