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

  public MessageDBStorage(QueryExecutor queryExecutor) {
    this.queryExecutor = queryExecutor;
  }

  @Override
  public void addMessage(Message message) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, message.getAuthorId());
    args.put(2, message.getChatId());
    args.put(3, message.getTime());
    args.put(4, message.getBody());

    queryExecutor.updateQuery("INSERT INTO \"messages\" (author_id, chat_id, time_sent, body) VALUES (?, ?, ?, ?);", args, (r) -> {
      //TODO: Write something that makes sence
      if(r.next()) {
        message.setId(r.getLong(1));
      }
      return true;
    });
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
    args.put(2, "%" + regex + "%");
    if (chat_id != null) {
      args.put(3, chat_id);
      query = "SELECT body FROM \"messages\" WHERE author_id = ? AND body LIKE ? AND chat_id = ?;";
    } else {
      query = "SELECT body FROM \"messages\" WHERE author_id = ? AND body LIKE ? AND chat_id IS NULL;";
    }

    return queryExecutor.execQuery(query, args, (r) -> {
      StringBuilder builder = new StringBuilder();
      while (r.next()) {
        builder.append("\n  ").append(r.getString("body"));
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

    query.append("SELECT * FROM (SELECT m.id, nickname, body FROM \"messages\" as m LEFT JOIN \"users\" as u ON m.author_id = u.id WHERE");
    int argid = 1;
    if (user != null) {
      args.put(argid++, user.getId());
      query.append(" author_id = ? AND");
    }
    if (chat_id != null) {
      args.put(argid++, chat_id);
      query.append(" chat_id = ?");
    } else {
      query.append(" chat_id IS NULL");
    }
    if (size > 0) {
      query.append(" ORDER BY m.id ASC LIMIT ?");
      args.put(argid++, size);
    }
    query.append(") sub ORDER BY id DESC;");

    return queryExecutor.execQuery(query.toString(), args, (r) -> {
      StringBuilder builder = new StringBuilder();
      while (r.next()) {
        builder.append("\n  ");
        if (user == null) {
          builder.append(r.getString("nickname")).append(": ");
        }
        builder.append(r.getString("body"));
      }
      if (builder.length() == 0)
        return "No Matches";
      else
        return builder.toString();
    });
  }

  @Override
  public void close() {
  }
}
