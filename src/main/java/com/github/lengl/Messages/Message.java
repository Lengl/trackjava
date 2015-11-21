package com.github.lengl.Messages;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class Message implements Serializable {
  private static volatile long idCounter = 0;
  private final long id;
  private long senderId;
  private String author;
  private Long authorId;
  private Long chatId;
  private String body;
  private Timestamp time;

  public Message(@NotNull String body, @NotNull Timestamp time) {
    this.id = idCounter++;
    this.body = body;
    this.time = time;
  }

  public Message(@NotNull String body) {
    this(body, new Timestamp(new java.util.Date().getTime()));
  }

  public Message(@NotNull String body, @NotNull String author) {
    this.id = idCounter++;
    this.body = body;
    this.author = author;
    this.time = new Timestamp(new java.util.Date().getTime());
  }

  @NotNull
  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
    this.time = new Timestamp(new java.util.Date().getTime());
  }

  @NotNull
  public Timestamp getTime() {
    return time;
  }

  @Nullable
  public Long getSenderId() {
    return senderId;
  }

  @Nullable
  public Long getChatId() {
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

  @Nullable
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public long getAuthorId() {
    return authorId;
  }

  public void setAuthorId(long authorId) {
    this.authorId = authorId;
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
