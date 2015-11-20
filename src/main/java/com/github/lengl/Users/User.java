package com.github.lengl.Users;

import com.sun.istack.internal.NotNull;

import java.util.Objects;

public class User {
  private long id;
  private String login;
  private String nickname;

  public User(String login, long id) {
    this.login = login;
    this.nickname = login;
    this.id = id;
  }

  @NotNull
  public String getLogin() {
    return login;
  }

  @NotNull
  public String getNickname() {
    return nickname;
  }

  public void setNickname(@NotNull String nickname) {
    this.nickname = nickname;
  }

  public long getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
