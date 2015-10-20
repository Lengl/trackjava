package com.github.lengl.Authorization;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.IOException;

public interface PasswordStorable {

  @Nullable
  User findUser(@NotNull String login);

  void add(@NotNull User user, @NotNull String pass) throws IOException;

  @NotNull
  boolean check(@NotNull User user, @NotNull String pass);

  void close();
}
