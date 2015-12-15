package com.github.lengl.Users;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

public interface UserStorable {

  @NotNull
  User create(@NotNull String login) throws Exception;

  @Nullable
  User findUserByLogin(@NotNull String login) throws Exception;

  @Nullable
  User findUserById(@NotNull long id) throws Exception;

  @NotNull
  String changeNickname(@NotNull Long id, @NotNull String newNickname) throws Exception;

  void close();
}
