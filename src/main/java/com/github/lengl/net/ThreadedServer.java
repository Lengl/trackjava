package com.github.lengl.net;

import com.github.lengl.Authorization.AuthorisationService;
import com.github.lengl.Authorization.PasswordDBStorage;
import com.github.lengl.ChatRoom.ChatRoomDBStorage;
import com.github.lengl.ChatRoom.ChatRoomStorable;
import com.github.lengl.Messages.InputHandler;
import com.github.lengl.Messages.Message;
import com.github.lengl.Messages.MessageDBStorage;
import com.github.lengl.Messages.MessageStorable;
import com.github.lengl.Messages.ServerMessageService;
import com.github.lengl.Messages.ServerMessages.AuthMessage;
import com.github.lengl.Messages.ServerMessages.QuitMessage;
import com.github.lengl.Messages.ServerMessages.ResponseMessage;
import com.github.lengl.Users.UserDBStorage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ThreadedServer implements MessageListener, Server {

  public static final int PORT = 8123;
  private final Logger log = Logger.getLogger(ThreadedServer.class.getName());
  private ServerSocket sSocket;
  private volatile boolean isRunning;

  private final Map<Long, ConnectionHandler> handlers = new HashMap<>();
  private final Map<Long, Thread> handlerThreads = new HashMap<>();

  //Map <authorizedUserID, handlerId>
  private final Map<Long, Long> authorisedHandlers = new HashMap<>();
  //Map <handlerId, handler>
  private final Map<Long, InputHandler> inputHandlers = new HashMap<>();

  private final AtomicLong internalCounterID = new AtomicLong(0);
  private Resources resources;

  public ThreadedServer() {
    try {
      sSocket = new ServerSocket(PORT);
      sSocket.setReuseAddress(true);
    } catch (IOException e) {
      log.log(Level.SEVERE, "IO Exception: ", e);
      System.exit(0);
    }
  }

  public static void main(String[] args) throws Exception {
    try {
      LogManager.getLogManager().readConfiguration(
          ThreadedServer.class.getResourceAsStream("/logging.properties"));
    } catch (IOException e) {
      System.err.println("Logger error. Server shutdown.");
      System.exit(0);
    }
    ThreadedServer server = new ThreadedServer();
    server.startServer();
  }

  public void startServer() {
    log.info("Started, waiting for connection");
    try {
      resources = new Resources();
    } catch (Exception e) {
      log.log(Level.SEVERE, "Unable to open resources: ", e);
      System.exit(0);
    }

    isRunning = true;
    while (isRunning) {
      try {
        Socket socket = sSocket.accept();
        log.info("Accepted. " + socket.getInetAddress());
        ConnectionHandler handler = new SocketConnectionHandler(socket);
        handler.addListener(this);


        long senderId = internalCounterID.incrementAndGet();
        handlers.put(senderId, handler);
        inputHandlers.put(senderId, new ServerMessageService(resources));
        Thread thread = new Thread(handler);
        thread.start();
        handlerThreads.put(senderId, thread);

        //Send to client it's ID
        handler.send(new Message(String.valueOf(senderId)));
      } catch (IOException e) {
        log.log(Level.SEVERE, "IO Exception: ", e);
      }
    }
  }

  //TODO: Find a way to reach this function
  public void destroyServer() {
    isRunning = false;
    Set<Long> keys = handlers.keySet();
    keys.forEach(key -> closeConnection(key));
    resources.close();
  }

  private void closeConnection(long id) {
    handlers.remove(id);
    inputHandlers.remove(id);
    authorisedHandlers.values().remove(id);
    handlerThreads.get(id).interrupt();
    handlerThreads.remove(id);
    log.info("Connection " + id + "closed.");
  }

  @Override
  public void onMessage(Message message) {
    //This message from client has: id, senderId, body, time
    long id = message.getSenderId();
    Message ret = inputHandlers.get(id).react(message);
    if (ret instanceof ResponseMessage) {
      try {

        handlers.get(id).send(ret);

        if (ret instanceof QuitMessage) {
          closeConnection(id);
        }
        if (ret instanceof AuthMessage) {
          if(authorisedHandlers.values().contains(id))
            authorisedHandlers.values().remove(id);
          authorisedHandlers.put(((AuthMessage) ret).getAuthorized().getId(), id);
        }

      } catch (IOException e) {
        log.log(Level.SEVERE, "Unable to send message", e);
        closeConnection(id);
      }
    } else {
      if (message.getChatId() == null) {

        for (ConnectionHandler handler : handlers.values()) {
          try {
            handler.send(message);
          } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to send message", e);
          }
        }

      } else {
        try {
          Set<Long> participants = resources.chatRoomStorage.getParticipantIDs(message.getChatId());
          if (participants != null) {
            if (!participants.contains(message.getAuthorId())) {
              //He doesn't belong chat
              try {
                handlers.get(id).send(new ResponseMessage("You don't belong to this chat!"));
              } catch (IOException e) {
                log.log(Level.SEVERE, "Unable to send message", e);
                closeConnection(id);
              }
            } else {
              //Send message to everyone in chat
              participants.forEach(participant -> {
                Long handlerId = authorisedHandlers.get(participant);
                if (handlerId != null) {
                  try {
                    handlers.get(handlerId).send(message);
                  } catch (IOException e) {
                    log.log(Level.SEVERE, "Unable to send message", e);
                    closeConnection(id);
                  }
                }
              });
            }
          } else {
            //Trying to send message in the chat that doesn't exist
            try {
              handlers.get(id).send(new ResponseMessage("Chat doesn't exist"));
            } catch (IOException e) {
              log.log(Level.SEVERE, "Unable to send message", e);
              closeConnection(id);
            }
          }
        } catch (Exception ex) {
          log.log(Level.SEVERE, "Unable to receive chat participants", ex);
        }
      }
    }
  }

}
