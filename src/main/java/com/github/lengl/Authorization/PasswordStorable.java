package com.github.lengl.Authorization;

import com.github.lengl.Users.User;
import com.sun.istack.internal.NotNull;

import java.io.IOException;

public interface PasswordStorable {

  void add(@NotNull User user, @NotNull String pass) throws IOException;

  @NotNull
  boolean check(@NotNull User user, @NotNull String pass);

  void close();
}
