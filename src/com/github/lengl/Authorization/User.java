package com.github.lengl.Authorization;

import com.sun.istack.internal.NotNull;

public class User {
  private final String login;
  private String nickname;

  public User(String login) {
    this.login = login;
    this.nickname = login;
  }

  @NotNull
  public String getLogin() {
    return login;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }
}
