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

  public PasswordDBStorage() throws Exception {
    queryExecutor = new QueryExecutor();
    queryExecutor.initialize();
  }

  @Override
  public void add(@NotNull User user, @NotNull String pass) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, user.getId());
    args.put(2, Encoder.encode(pass));

    queryExecutor.updateQuery(
        "INSERT INTO \"passwords\" (user_id, password) VALUES (?, ?);",
        args);
  }

  @Override
  public boolean check(@NotNull User user, @NotNull String pass) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, user.getId());
    args.put(2, Encoder.encode(pass));

    return queryExecutor.execQuery("SELECT * FROM \"passwords\" WHERE user_id = ? AND password = ?;", args, (r) -> {
      return r.next();
    });
  }

  @Override
  public void close() {
    queryExecutor.exit();
  }
}
