package com.github.lengl.net;

import com.github.lengl.Authorization.AuthorisationService;
import com.github.lengl.ChatRoom.ChatRoom;
import com.github.lengl.Messages.InputHandler;
import com.github.lengl.Messages.Message;
import com.github.lengl.Messages.MessageService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ThreadedServer implements MessageListener {

  public static final int PORT = 8123;
  private final Logger log = Logger.getLogger(ThreadedServer.class.getName());
  private ServerSocket sSocket;
  private volatile boolean isRunning;
  private final Map<Long, ConnectionHandler> handlers = new HashMap<>();
  private final Map<Long, Thread> handlerThreads = new HashMap<>();
  private final Map<Long, InputHandler> inputHandlers = new HashMap<>();
  private Map<Long, ChatRoom> chatRooms = new HashMap<>();
  private final AtomicLong internalCounterID = new AtomicLong(0);
  private AuthorisationService authorisationService;

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

  public void startServer() throws Exception {
    log.info("Started, waiting for connection");

    isRunning = true;
    while (isRunning) {
      Socket socket = sSocket.accept();
      log.info("Accepted. " + socket.getInetAddress());
      ConnectionHandler handler = new SocketConnectionHandler(socket);
      handler.addListener(this);

      authorisationService = new AuthorisationService();

      long senderId = internalCounterID.incrementAndGet();
      handlers.put(senderId, handler);
      inputHandlers.put(senderId, new MessageService());
      Thread thread = new Thread(handler);
      thread.start();
      handlerThreads.put(senderId, thread);

      //Send to client it's ID
      handler.send(new Message(String.valueOf(senderId)));
    }
  }

  public void stopServer() {
    isRunning = false;
    handlers.values().forEach(com.github.lengl.net.ConnectionHandler::stop);
    authorisationService.stop();
  }

  private void closeConnection(long id) {
    handlers.remove(id);
    inputHandlers.remove(id);
    handlerThreads.get(id).interrupt();
    handlerThreads.remove(id);
    log.info("Connection " + id + "closed.");
  }

  @Override
  public void onMessage(Message message) {
    long id = message.getSenderId();
    String ret = inputHandlers.get(id).react(message.getBody());
    if (ret != null) {

      try {
        Message response = new Message(ret);
        response.setAuthor("server");
        handlers.get(id).send(response);
      } catch (IOException e) {
        log.log(Level.SEVERE, "Unable to send message", e);
        closeConnection(id);
      }

      //TODO: Get rid of duplicated check
      if ("/quit".equals(message.getBody().trim()) || "/q".equals(message.getBody().trim())) {
        closeConnection(id);
      }

    } else {
      message.setAuthor(inputHandlers.get(message.getSenderId()).getAuthor());
      for (ConnectionHandler handler : handlers.values()) {

        try {
          handler.send(message);
        } catch (IOException e) {
          log.log(Level.SEVERE, "Unable to send message", e);
        }

      }
    }
  }
}
