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
    this(body, new Timestamp(new java.util.Date().getTime()));
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
    return "Author=<" + getAuthor() + ">" +
        ", Message=<" + body + ">";
  }

  public void setSenderId(long senderId) {
    this.senderId = senderId;
  }

  public String getAuthor() {
    if (author == null)
      return "unknownUser" + senderId;
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
}
