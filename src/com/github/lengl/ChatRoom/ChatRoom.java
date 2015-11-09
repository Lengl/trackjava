package com.github.lengl.ChatRoom;

import com.github.lengl.Messages.MessageStorable;
import com.github.lengl.Messages.MessageStorage;
import com.github.lengl.Users.User;

import java.io.IOException;
import java.util.List;

public class ChatRoom {
  private final long id;
  private List<User> participants;
  public final MessageStorable messageStorage;

  public ChatRoom(long id) throws IOException {
    this.id = id;
    this.messageStorage = new MessageStorage(id);
  }

  public void addParticipant(User user) {
    participants.add(user);
  }

  public void removeParticipant(User user) {
    participants.remove(user);
  }

  boolean hasParticipant(User user) {
    return participants.contains(user);
  }
}
