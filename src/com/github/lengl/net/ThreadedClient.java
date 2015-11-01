package com.github.lengl.net;

import com.github.lengl.Messages.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ThreadedClient implements MessageListener{

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
    } catch (IOException e) {
      e.printStackTrace();
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
    ThreadedClient client = new ThreadedClient();

    Scanner scanner = new Scanner(System.in);
    System.out.println("$");
    while (true) {
      String input = scanner.next();
      if ("/q".equals(input)) {
        return;
      }
      client.processInput(input);
    }
  }
}
