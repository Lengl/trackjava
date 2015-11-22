package com.github.lengl.Authorization;

import com.github.lengl.Users.User;
import com.sun.istack.internal.NotNull;

public interface PasswordStorable {

  void add(@NotNull User user, @NotNull String pass) throws Exception;

  @NotNull
  boolean check(@NotNull User user, @NotNull String pass) throws Exception;

  String changePassword(@NotNull User user, @NotNull String pass) throws Exception;

  void close();
}
