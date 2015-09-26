package com.github.lengl.Authorization;

public class User {
  private final String name;
  private final String password;

  public User(String name, String password) {
    this.name = name;
    this.password = password;
  }

  public boolean nameIs(String name) {
    return this.name.equals(name);
  }

  public boolean passwordIs(String password) {
    return this.password.equals(password);
  }
}
