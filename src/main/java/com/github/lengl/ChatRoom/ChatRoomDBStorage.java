package com.github.lengl.ChatRoom;

import com.github.lengl.jdbc.QueryExecutable;
import com.github.lengl.jdbc.QueryExecutor;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChatRoomDBStorage implements ChatRoomStorable {
  private final QueryExecutable queryExecutor;

  private final Map<Long, ChatRoom> allChats = new HashMap<>();

  public ChatRoomDBStorage() throws Exception {
    queryExecutor = new QueryExecutor();
    queryExecutor.initialize();
    queryExecutor.execQuery("SELECT chat_id, participant_id FROM \"chatrooms\" as c LEFT JOIN \"chatroom_users\" as cu ON c.id = cu.chat_id;", (r) -> {
      long chat_id = -1;
      ChatRoom chatRoom = null;
      while (r.next()) {
        if (r.getLong("chat_id") != chat_id) {
          if (chatRoom != null)
            allChats.put(chat_id, chatRoom);
          chat_id = r.getLong("chat_id");
          chatRoom = new ChatRoom(r.getLong("chat_id"));
        }
        if (chatRoom != null) {
          chatRoom.addParticipant(r.getLong("participant_id"));
        }
      }
      if (chatRoom != null)
        allChats.put(chat_id, chatRoom);
      return true;
    });
  }

  @Nullable
  //Returns new ChatRoom ID if successfull.
  public Long createChatRoom() throws Exception {
    return queryExecutor.updateQuery("INSERT INTO \"chatrooms\" DEFAULT VALUES;", new HashMap<>(), (r) -> {
      if (r.next()) {
        allChats.put(r.getLong(1), new ChatRoom(r.getLong(1)));
        return r.getLong(1);
      } else {
        return null;
      }
    });
  }

  @NotNull
  public String addParticipant(Long roomId, Long userId) throws Exception {
    ChatRoom room;
    if (allChats.containsKey(roomId)) {
      room = allChats.get(roomId);
    } else {
      return "Chat doesn't exist";
    }
    if (!room.hasParticipant(userId)) {
      Map<Integer, Object> args = new HashMap<>();
      args.put(1, roomId);
      args.put(2, userId);

      String ret = queryExecutor.updateQuery("INSERT INTO \"chatroom_users\" (chat_id, participant_id) VALUES (?, ?);", args, (r) -> "User added successfully");
      room.addParticipant(userId);
      return ret;
    } else {
      return "Already in this chat";
    }
  }

  @NotNull
  public String removeParticipant(Long roomId, Long userId) throws Exception {
    ChatRoom room;
    if (allChats.containsKey(roomId)) {
      room = allChats.get(roomId);
    } else {
      return "Chat doesn't exist";
    }
    if (room.hasParticipant(userId)) {
      Map<Integer, Object> args = new HashMap<>();
      args.put(1, roomId);
      args.put(2, userId);
      //TODO: Write something that makes more sence
      String ret = queryExecutor.execQuery("DELETE FROM \"chatroom_users\" WHERE chat_id = ? AND participant_id = ?;", args, (r) -> "User removed successfully");
      room.removeParticipant(userId);
      return ret;
    } else {
      return "User doesn't belong this chat";
    }
  }

  @Nullable
  public Set<Long> getParticipantIDs(Long roomId) throws Exception {
    ChatRoom room;
    if (allChats.containsKey(roomId)) {
      room = allChats.get(roomId);
    } else {
      return null;
    }
    return room.getParticipantIDs();
  }

  @Override
  public Set<Long> getChatsFromUser(Long userId) throws Exception {
    Map<Integer, Object> args = new HashMap<>();
    args.put(1, userId);
    return queryExecutor.execQuery("SELECT chat_id FROM \"chatroom_users\" WHERE participant_id = ?;", args, (r) -> {
      Set<Long> ret = new HashSet<>();
      while (r.next()) {
        ret.add(r.getLong("chat_id"));
      }
      return ret;
    });
  }

  @Override
  public boolean isParticipant(Long roomId, Long userId) throws Exception {
    return allChats.containsKey(roomId) && allChats.get(roomId).hasParticipant(userId);
  }

  public void close() {
    queryExecutor.exit();
  }
}
