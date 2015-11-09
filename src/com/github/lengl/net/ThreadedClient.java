package com.github.lengl.net;

import com.github.lengl.Messages.Message;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ThreadedClient implements MessageListener {

  public static final int PORT = 8123;
  public static final String HOST = "localhost";
  private final Logger log = Logger.getLogger(ThreadedClient.class.getName());
  ConnectionHandler handler;
  Thread socketHandlerThread;
  private long myId = -1;

  public ThreadedClient() {
    try {
      Socket socket = new Socket(HOST, PORT);
      handler = new SocketConnectionHandler(socket);

      handler.addListener(this);

      socketHandlerThread = new Thread(handler);
      socketHandlerThread.start();
    } catch (UnknownHostException e) {
      log.log(Level.SEVERE, "Unknown host: ", e);
      System.err.println("Host exception. Restart the client");
      System.exit(0);
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOExceptopn: ", e);
      System.err.println("IO exception. Restart the client");
      System.exit(0);
    }
  }

  public static void main(String[] args) throws Exception {
    try {
      LogManager.getLogManager().readConfiguration(
          ThreadedClient.class.getResourceAsStream("/logging.properties"));
    } catch (IOException e) {
      System.err.println("Logger error. Please restart the client.");
      System.exit(0);
    }
    ThreadedClient client = new ThreadedClient();

    Scanner scanner = new Scanner(System.in);
    System.out.println("Waiting for connection...");
    //TODO: Add waiting while myId == -1
    System.out.println("Connected.");
    while (true) {
      String input = scanner.nextLine();
      client.processInput(input);
      if ("/q".equals(input) || "/quit".equals(input)) {
        client.socketHandlerThread.interrupt();
        return;
      }
    }
  }

  public void processInput(String line) throws IOException {
    Message msg = new Message(line);
    msg.setSenderId(myId);
    handler.send(msg);
  }

  @Override
  public void onMessage(Message message) {
    if (myId == -1) {
      try {
        myId = Long.parseLong(message.getBody());
      } catch (NumberFormatException e) {
        //This is something REALLY unexpected
        //Because first message we get from server should be our ID.
        log.log(Level.SEVERE, "First message wasn't the ID!");
        System.err.println("Unexpected problems. Restart the client");
        System.exit(0);
      }
    } else {
      System.out.printf("%s\n", message);
    }
  }
}
