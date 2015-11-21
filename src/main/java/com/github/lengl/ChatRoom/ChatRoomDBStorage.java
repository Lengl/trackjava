package com.github.lengl.ChatRoom;

import com.github.lengl.Users.User;
import com.github.lengl.jdbc.QueryExecutable;
import com.github.lengl.jdbc.QueryExecutor;
import com.sun.istack.internal.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomDBStorage {
  private final QueryExecutable queryExecutor;

  public ChatRoomDBStorage() throws Exception {
    queryExecutor = new QueryExecutor();
    queryExecutor.initialize();
  }

  @Nullable
  public ChatRoom createChatRoom() throws Exception {
    return queryExecutor.updateQuery("INSERT INTO \"chatrooms\" DEFAULT VALUES;", new HashMap<>(), (r) -> {
      if (r.next()) {
        return new ChatRoom(r.getLong(1));
      } else {
        return null;
      }
    });
  }

  public String addParticipant(ChatRoom room, User user) throws Exception {
    if (!room.hasParticipant(user)) {
      room.addParticipant(user);

      Map<Integer, Object> args = new HashMap<>();
      args.put(1, room.getId());
      args.put(2, user.getId());

      return queryExecutor.execQuery("INSERT INTO \"chatroom_users\" (chat_id, participant_id) VALUES (?, ?);", args, (r) -> "User added successfully");
    } else {
      return "Already in this chat";
    }
  }

  public String removeParticipant(ChatRoom room, User user) throws Exception {
    if (room.hasParticipant(user)) {
      room.removeParticipant(user);
      Map<Integer, Object> args = new HashMap<>();
      args.put(1, room.getId());
      args.put(2, user.getId());
      //TODO: Write something that makes more sence
      return queryExecutor.execQuery("DELETE FROM \"chatroom_users\" WHERE chat_id = ? AND participant_id = ?;", args, (r) -> {
        return "User removed successfully";
      });
    } else {
      return "User doesn't belong this chat";
    }
  }

  public ChatRoom getChat(long id) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, id);
    return queryExecutor.execQuery("SELECT count(*) FROM \"chatrooms\" WHERE id = ?", args, (r) -> {
      r.next();
      if (r.getLong(1) == 1)
        return new ChatRoom(r.getLong(1));
      else
        return null;
    });
  }
}
