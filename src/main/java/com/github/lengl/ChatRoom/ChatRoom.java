package com.github.lengl.ChatRoom;

import com.github.lengl.Users.User;

import java.util.HashSet;
import java.util.Set;

public class ChatRoom {
  protected static volatile long idCounter = 0;
  protected final long id;
  protected final Set<User> participants = new HashSet<>();

  public ChatRoom() {
    this.id = idCounter++;
  }

  public ChatRoom(long id) {
    this.id = id;
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

  public Set<User> getParticipants() {
    return participants;
  }

  public long getId() {
    return id;
  }
}
