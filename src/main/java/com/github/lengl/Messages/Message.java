package com.github.lengl.Messages;

import com.sun.istack.internal.NotNull;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class Message implements Serializable {
  private long id;
  private long senderId;
  private long chatId = -1;
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

  public Message(@NotNull String body, @NotNull String author) {
    this.body = body;
    this.author = author;
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

  public long getChatId() {
    return chatId;
  }

  public void setChatId(long chatId) {
    this.chatId = chatId;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Message message = (Message) o;
    return Objects.equals(id, message.id) &&
        Objects.equals(senderId, message.senderId) &&
        Objects.equals(chatId, message.chatId) &&
        Objects.equals(author, message.author) &&
        Objects.equals(body, message.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, senderId, chatId, author, body);
  }
}
