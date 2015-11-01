package com.github.lengl.net;

import com.github.lengl.Messages.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketConnectionHandler implements ConnectionHandler{
  private final Logger log = Logger.getLogger(SocketConnectionHandler.class.getName());
  private List<MessageListener> listeners = new ArrayList<>();
  private Socket socket;
  private InputStream in;
  private OutputStream out;


  public SocketConnectionHandler(Socket socket) throws IOException {
    this.socket = socket;
    in = socket.getInputStream();
    out = socket.getOutputStream();
  }

  @Override
  public void send(Message message) throws IOException {
    out.write(Protocol.encode(message));
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
    Thread.currentThread().interrupt();
  }

  @Override
  public void run() {
    final byte[] buffer = new byte[1024 * 64];

    while(!Thread.currentThread().isInterrupted()) {
      try{
        int size = in.read(buffer);

        if (size > 0) {
          Message msg = Protocol.decode(Arrays.copyOf(buffer, size));
          log.info("Message recieved:" + msg);
          notifyListeners(msg);
        }
      } catch (IOException e) {
        log.log(Level.SEVERE, "Failed to handle connection:", e);
        Thread.currentThread().interrupt();
      }
    }
  }
}
