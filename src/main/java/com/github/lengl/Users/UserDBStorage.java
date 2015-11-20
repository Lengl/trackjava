package com.github.lengl.Users;


import com.github.lengl.jdbc.QueryExecutor;
import com.sun.istack.internal.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class UserDBStorage implements UserStorable {
  private final Logger log = Logger.getLogger(UserFileStorage.class.getName());

  private QueryExecutor queryExecutor;

  public UserDBStorage() throws ClassNotFoundException {
    queryExecutor = new QueryExecutor();
    queryExecutor.initialize();
  }

  @Override
  public User create(@NotNull String login) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    //this one for login
    args.put(1, login);
    //this one for default nickname==login
    args.put(2, login);

    long id = queryExecutor.updateQuery(
        "INSERT INTO \"users\" (login, nickname) VALUES (?, ?);",
        args);
    return new User(login, id);
  }

  @Override
  public User findUserByLogin(@NotNull String login) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, login);

    return queryExecutor.execQuery("SELECT * FROM \"users\" WHERE login = ?;", args, (r) -> {
      r.next();
      return new User(r.getString("login"), r.getLong("id"));
    });
  }

  @Override
  public User findUserById(@NotNull long id) throws SQLException {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, id);

    return queryExecutor.execQuery("SELECT * FROM \"users\" WHERE id = ?;", args, (r) -> {
      r.next();
      return new User(r.getString("login"), r.getLong("id"));
    });
  }

  @Override
  public void close() {
    queryExecutor.exit();
  }
}
