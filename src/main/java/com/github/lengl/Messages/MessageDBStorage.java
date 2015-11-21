package com.github.lengl.Messages;

import com.github.lengl.Users.User;
import com.github.lengl.Users.UserFileStorage;
import com.github.lengl.jdbc.QueryExecutable;
import com.github.lengl.jdbc.QueryExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MessageDBStorage implements MessageStorable {
  private final Logger log = Logger.getLogger(UserFileStorage.class.getName());

  private final QueryExecutable queryExecutor;

  public MessageDBStorage() throws Exception {
    queryExecutor = new QueryExecutor();
    queryExecutor.initialize();
  }

  @Override
  public void addMessage(Message message) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, message.getAuthorId());
    args.put(2, message.getChatId());
    args.put(3, message.getTime());
    args.put(4, message.getBody());

    queryExecutor.updateQuery(
        "INSERT INTO \"messages\" (author_id, chat_id, time_sent, body) VALUES (?, ?, ?, ?);",
        args);
  }

  @Override
  public String findMessage(String regex, User user) throws Exception {
    return findMessage(regex, user, null);
  }

  @Override
  public String findMessage(String regex, User user, Long chat_id) throws Exception {
    //TODO: Add different tables for different chats
    Map<Integer, Object> args = new HashMap<>();
    String query;
    args.put(1, user.getId());
    args.put(2, regex);
    if (chat_id != null) {
      args.put(3, chat_id);
      query = "SELECT body FROM \"messages\" WHERE author_id = ? AND body LIKE ? AND chat_id = ?;";
    } else {
      query = "SELECT body FROM \"messages\" WHERE author_id = ? AND body LIKE ? AND chat_id IS NULL;";
    }

    return queryExecutor.execQuery(query, args, (r) -> {
      StringBuilder builder = new StringBuilder();
      while (r.next()) {
        builder.append(r.getString("body")).append("\n");
      }
      if (builder.length() == 0)
        return "No Matches";
      else
        return builder.toString();
    });
  }

  @Override
  public String getHistory(int size, User user) throws Exception {
    return getHistory(size, user, null);
  }

  @Override
  public String getHistory(int size, User user, Long chat_id) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    StringBuilder query = new StringBuilder();
    query.append("SELECT id, body FROM \"messages\" WHERE author_id = ?");
    int argid = 1;
    args.put(argid++, user.getId());
    if (chat_id != null) {
      args.put(argid++, chat_id);
      query.append(" AND chat_id = ?");
    } else {
      query.append(" AND chat_id IS NULL");
    }
    query.append("ORDER BY id");
    if (size > 0) {
      query.append(" LIMIT ?");
      args.put(argid++, size);
    }
    query.append(";");
    return queryExecutor.execQuery(query.toString(), args, (r) -> {
      StringBuilder builder = new StringBuilder();
      while (r.next()) {
        builder.append(r.getString("body")).append("\n");
      }
      if (builder.length() == 0)
        return "No Matches";
      else
        return builder.toString();
    });
  }

  @Override
  public void close() {
    queryExecutor.exit();
  }
}
