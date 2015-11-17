package com.github.lengl.ChatRoom;

import com.github.lengl.Messages.MessageFileStorage;
import com.github.lengl.Messages.MessageStorable;
import com.github.lengl.Users.User;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ChatRoom {
  private static volatile long idCounter = 0;
  private final long id;
  private Set<User> participants = new HashSet<>();
  public final MessageStorable messageStorage;

  public ChatRoom() throws IOException {
    this.id = idCounter++;
    this.messageStorage = new MessageFileStorage(id);
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
