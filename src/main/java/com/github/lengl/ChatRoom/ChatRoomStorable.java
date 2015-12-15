package com.github.lengl.ChatRoom;

import java.util.Set;

public interface ChatRoomStorable {
  Long createChatRoom() throws Exception;

  String addParticipant(Long roomId, Long userId) throws Exception;

  String removeParticipant(Long roomId, Long userId) throws Exception;

  Set<Long> getParticipantIDs(Long roomId) throws Exception;

  Set<Long> getChatsFromUser(Long userId) throws Exception;

  boolean isParticipant(Long roomId, Long userId) throws Exception;

  void close();
}
