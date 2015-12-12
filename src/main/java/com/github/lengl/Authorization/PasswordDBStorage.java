package com.github.lengl.Authorization;

import com.github.lengl.Users.User;
import com.github.lengl.Users.UserFileStorage;
import com.github.lengl.jdbc.QueryExecutable;
import com.github.lengl.jdbc.QueryExecutor;
import com.sun.istack.internal.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PasswordDBStorage implements PasswordStorable {
  private final Logger log = Logger.getLogger(UserFileStorage.class.getName());

  private final QueryExecutable queryExecutor;

  public PasswordDBStorage(QueryExecutor queryExecutor) {
    this.queryExecutor = queryExecutor;
  }

  @Override
  public void add(@NotNull User user, @NotNull String pass) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, user.getId());
    args.put(2, Encoder.encode(pass));

    queryExecutor.updateQuery("INSERT INTO \"passwords\" (user_id, password) VALUES (?, ?);", args, (r) -> {
      //TODO: Write something that makes sence.
      return true;
    });
  }

  @Override
  public boolean check(@NotNull User user, @NotNull String pass) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, user.getId());
    args.put(2, Encoder.encode(pass));

    return queryExecutor.execQuery("SELECT * FROM \"passwords\" WHERE user_id = ? AND password = ?;", args, (r) -> r.next());
  }

  @Override
  public String changePassword(@NotNull User user, @NotNull String pass) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, Encoder.encode(pass));
    args.put(2, user.getId());
    return queryExecutor.updateQuery("UPDATE \"passwords\" SET password = ? WHERE user_id = ?;", args, (r) -> "Password changed successfully");
  }

  @Override
  public void close() {
  }
}
