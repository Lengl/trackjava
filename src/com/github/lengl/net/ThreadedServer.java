package com.github.lengl.net;

import com.github.lengl.Messages.Message;

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

  private final Logger log = Logger.getLogger(ThreadedServer.class.getName());
  private ServerSocket sSocket;
  private boolean isRunning;
  private Map<Long, ConnectionHandler> handlers = new HashMap<>();
  private AtomicLong internalCounter = new AtomicLong(0);

  public static final int PORT = 8123;

  public ThreadedServer() {
    try {
      sSocket = new ServerSocket(PORT);
      sSocket.setReuseAddress(true);
    } catch (IOException e) {
      log.log(Level.SEVERE, "IO Exception: ", e);
      System.exit(0);
    }
  }

  public void startServer() throws Exception {
    log.info("Started, waiting for connection");

    isRunning = true;
    while (isRunning) {
      Socket socket = sSocket.accept();
      log.info("Accepted. " + socket.getInetAddress());
      ConnectionHandler handler = new SocketConnectionHandler(socket);
      handler.addListener(this);

      handlers.put(internalCounter.incrementAndGet(), handler);
      Thread thread = new Thread(handler);
      thread.start();
    }
  }

  public void stopServer() {
    isRunning = false;
    for (ConnectionHandler handler : handlers.values()) {
      handler.stop();
    }
  }

  @Override
  public void onMessage(Message message) {
    try {
      for (ConnectionHandler handler : handlers.values()) {
        handler.send(message);
      }
    } catch (Exception e) {
      log.log(Level.INFO, "Unable to send message:", e);
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
}
