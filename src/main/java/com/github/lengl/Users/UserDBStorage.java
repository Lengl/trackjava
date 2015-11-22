package com.github.lengl.Users;


import com.github.lengl.jdbc.QueryExecutable;
import com.github.lengl.jdbc.QueryExecutor;
import com.sun.istack.internal.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class UserDBStorage implements UserStorable {
  private final Logger log = Logger.getLogger(UserFileStorage.class.getName());

  private final QueryExecutable queryExecutor;

  public UserDBStorage() throws Exception {
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

    return queryExecutor.updateQuery("INSERT INTO \"users\" (login, nickname) VALUES (?, ?);", args, (r) -> {
      if (r.next()) {
        return new User(login, r.getLong(1));
      } else {
        return null;
      }
    });
  }

  @Override
  public User findUserByLogin(@NotNull String login) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, login);

    return queryExecutor.execQuery("SELECT * FROM \"users\" WHERE login = ?;", args, (r) -> {
      if (r.next())
        return new User(r.getString("login"), r.getLong("id"));
      else
        return null;
    });
  }

  @Override
  public User findUserById(@NotNull long id) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, id);

    return queryExecutor.execQuery("SELECT * FROM \"users\" WHERE id = ?;", args, (r) -> {
      if (r.next())
        return new User(r.getString("login"), r.getLong("id"));
      else
        return null;
    });
  }

  public String changeNickname(@NotNull Long id, @NotNull String newNickname) throws Exception {
    if (findUserById(id) != null) {
      Map<Integer, Object> args = new HashMap<>();
      args.put(1, newNickname);
      args.put(2, id);
      return queryExecutor.updateQuery("UPDATE \"users\" SET nickname = ? WHERE id = ?;", args, (r) -> "Nickname successfully changed");
    } else {
      return "User not found";
    }
  }

  @Override
  public void close() {
    queryExecutor.exit();
  }
}
