package com.github.lengl.net.nio;

import com.github.lengl.Messages.Message;
import com.github.lengl.Messages.ServerMessages.AuthMessage;
import com.github.lengl.Messages.ServerMessages.ResponseMessage;
import org.apache.commons.lang3.SerializationUtils;

import java.nio.channels.SocketChannel;
import java.util.Set;

public class MyWorker implements Runnable {
  private final NioServer server;
  private final SocketChannel socket;
  private final byte[] data;

  public MyWorker(NioServer server, SocketChannel socket, byte[] data, int count) {
    this.server = server;
    this.socket = socket;
    this.data = new byte[count];
    System.arraycopy(data, 0, this.data, 0, count);
  }

  @Override
  public void run() {
    // Return to sender
    Message message = (Message) SerializationUtils.deserialize(data);
    Message ret = server.inputHandlers.get(socket).react(message);

    if (ret instanceof ResponseMessage) {
      if (ret instanceof AuthMessage) {
        synchronized (server.authorisedClients) {
          if (server.authorisedClients.values().contains(socket)) {
            server.authorisedClients.values().remove(socket);
          }
          server.authorisedClients.put(((AuthMessage) ret).getAuthorized().getId(), socket);
        }
      }

      server.send(socket, SerializationUtils.serialize(ret));

    } else {
      //If it is a general message
      if (message.getChatId() == null) {
        server.inputHandlers.keySet().stream().forEach(ssocket -> {
          server.send(ssocket, SerializationUtils.serialize(ret));
        });
      } else {
        try {
          Set<Long> participants = server.resources.chatRoomStorage.getParticipantIDs(message.getChatId());
          if (participants != null) {
            if (participants.contains(message.getAuthorId())) {
              //Send message to everyone in chat
              participants.forEach(p -> {
                SocketChannel channel = server.authorisedClients.get(p);
                if (channel != null) {
                  server.send(channel, SerializationUtils.serialize(ret));
                }
              });
            } else {
              server.send(socket, SerializationUtils.serialize(
                      new ResponseMessage("You don't belong to this chat!"))
              );
            }
          } else {
            //Trying to send message in the chat that doesn't exist
            server.send(socket, SerializationUtils.serialize(
                new ResponseMessage("Chat doesn't exist.")
            ));
          }
        } catch (Exception ignored) {
        }
      }
    }
  }
}
