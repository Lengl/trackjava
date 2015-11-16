package com.github.lengl.Users;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.IOException;

public interface UserStorable {

  @NotNull
  User create(@NotNull String login) throws IOException;

  @Nullable
  User findUserByLogin(@NotNull String login);

  @Nullable
  User findUserById(@NotNull long id);

  void close();
}
