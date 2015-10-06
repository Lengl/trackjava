package com.github.lengl.Authorization;

import com.sun.istack.internal.NotNull;

public class User {
  private final String name;

  public User(String name) {
    this.name = name;
  }

  @NotNull
  public String getName() {
    return name;
  }
}
