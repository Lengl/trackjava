package com.github.lengl.Messages.ServerMessages;

import com.github.lengl.ChatRoom.ChatRoom;
import com.sun.istack.internal.NotNull;

public class ChatCreatedMessage extends ResponseMessage {
  private transient ChatRoom createdRoom;

  public ChatCreatedMessage(@NotNull String body, @NotNull ChatRoom room) {
    super(body);
    this.createdRoom = room;
  }

  public ChatRoom getCreatedRoom() {
    return createdRoom;
  }
}
