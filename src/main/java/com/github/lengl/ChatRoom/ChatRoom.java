package com.github.lengl.ChatRoom;

import java.util.HashSet;
import java.util.Set;

public class ChatRoom {
  protected static volatile long idCounter = 0;
  protected final long id;
  protected final Set<Long> participantIDs = new HashSet<>();

  protected ChatRoom() {
    this.id = idCounter++;
  }

  protected ChatRoom(long id) {
    this.id = id;
  }

  protected void addParticipant(Long userId) {
    participantIDs.add(userId);
  }

  protected void removeParticipant(Long userId) {
    participantIDs.remove(userId);
  }

  protected boolean hasParticipant(Long userId) {
    return participantIDs.contains(userId);
  }

  protected Set<Long> getParticipantIDs() {
    return participantIDs;
  }

  protected long getId() {
    return id;
  }
}
