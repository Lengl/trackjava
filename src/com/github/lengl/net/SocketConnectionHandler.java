package com.github.lengl.net;

import com.github.lengl.Messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketConnectionHandler implements ConnectionHandler {
  private final Logger log = Logger.getLogger(SocketConnectionHandler.class.getName());
  private List<MessageListener> listeners = new ArrayList<>();
  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;


  public SocketConnectionHandler(Socket socket) throws IOException {
    this.socket = socket;
    out = new ObjectOutputStream(socket.getOutputStream());
    in = new ObjectInputStream(socket.getInputStream());
  }

  @Override
  public void send(Message message) throws IOException {
    out.writeObject(message);
    out.flush();
  }

  @Override
  public void addListener(MessageListener listener) {
    listeners.add(listener);
  }

  public void notifyListeners(final Message message) {
    listeners.forEach(it -> it.onMessage(message));
  }

  @Override
  public void stop() {
    try {
      in.close();
      out.close();
      socket.close();
    } catch (IOException e) {
      log.log(Level.SEVERE, "Exception while closing socket", e);
    }
    log.info("Connection closed.");
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
      try {
        Message msg = (Message) in.readObject();
        log.info("Message recieved:" + msg);
        notifyListeners(msg);
      } catch (IOException e) {
        log.log(Level.SEVERE, "Failed to handle connection:", e);
        Thread.currentThread().interrupt();
      } catch (ClassNotFoundException e) {
        log.log(Level.SEVERE, "Failed to recognize object:", e);
      }
    }
    stop();
  }
}
