package com.github.lengl.net;

import com.github.lengl.Messages.Message;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ThreadedClient implements MessageListener{

  private final Logger log = Logger.getLogger(ThreadedClient.class.getName());

  public static final int PORT = 8123;
  public static final String HOST = "localhost";

  ConnectionHandler handler;

  public ThreadedClient() {
    try {
      Socket socket = new Socket(HOST, PORT);
      handler = new SocketConnectionHandler(socket);

      handler.addListener(this);

      Thread socketHandler = new Thread(handler);
      socketHandler.start();
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

  public void processInput(String line) throws IOException {
    Message msg = new Message(line);
    handler.send(msg);
  }

  @Override
  public void onMessage(Message message) {
    System.out.printf("%s", message);
  }

  public static void main(String[] args) throws Exception{
    try {
      LogManager.getLogManager().readConfiguration(
          ThreadedClient.class.getResourceAsStream("/logging.properties"));
    } catch (IOException e) {
      System.err.println("Logger error. Please restart the client.");
      System.exit(0);
    }
    ThreadedClient client = new ThreadedClient();

    Scanner scanner = new Scanner(System.in);
    System.out.println("$");
    while (true) {
      String input = scanner.next();
      if ("/q".equals(input) || "/quit".equals(input)) {
        return;
      }
      client.processInput(input);
    }
  }
}
