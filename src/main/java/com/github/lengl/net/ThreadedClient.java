package com.github.lengl.net;

import com.github.lengl.Messages.ClientMessageService;
import com.github.lengl.Messages.InputHandler;
import com.github.lengl.Messages.Message;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class ThreadedClient implements MessageListener {

  public static final int PORT = 8123;
  public static final String HOST = "localhost";
  private final Logger log = Logger.getLogger(ThreadedClient.class.getName());
  private ConnectionHandler handler;
  private Thread socketHandlerThread;
  private Thread inputThread;
  private volatile long myId = -1;
  private InputHandler inputHandler;

  public ThreadedClient() {
    try {
      Socket socket = new Socket(HOST, PORT);
      handler = new SocketConnectionHandler(socket);

      handler.addListener(this);

      socketHandlerThread = new Thread(handler);
      socketHandlerThread.start();

      inputThread = Thread.currentThread();

      inputHandler = new ClientMessageService();
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

  public static void main(String[] args) {
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
    //Wait one minute for connection
    try {
      sleep(60000);
      System.err.println("Server not responding. Restart the client.");
      client.log.log(Level.SEVERE, "No response from server in one minute");
      client.close();
      System.exit(0);
    } catch (InterruptedException e) {
      System.out.println("Connected.");
    }
    while (!Thread.currentThread().isInterrupted()) {
      String input = scanner.nextLine();
      client.processInput(input);
      if ("/q".equals(input) || "/quit".equals(input)) {
        client.socketHandlerThread.interrupt();
        return;
      }
    }
  }

  public void processInput(String line) {
    Message msg = new Message(line);
    msg.setSenderId(myId);
    msg = inputHandler.react(msg);
    if (msg != null) {
      try {
        handler.send(msg);
      } catch (IOException e) {
        log.log(Level.SEVERE, "Unable to send message: ", e);
      }
    }
  }

  @Override
  public void onMessage(Message message) {
    if (myId == -1) {
      try {
        myId = Long.parseLong(message.getBody());
        inputThread.interrupt();
      } catch (NumberFormatException e) {
        //This is something REALLY unexpected
        //Because first message we get from server should be our ID.
        log.log(Level.SEVERE, "First message wasn't the ID!");
        System.err.println("Unexpected problems. Restart the client");
      }
    } else {
      System.out.printf("%s: %s\n", message.getAuthor(), message.getBody());
    }
  }

  public void close() {
    socketHandlerThread.interrupt();
  }
}
