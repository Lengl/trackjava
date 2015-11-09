package com.github.lengl.Users;

import com.sun.istack.internal.NotNull;

public class User {
  private final long id;
  private final String login;
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
}
