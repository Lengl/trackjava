package com.github.lengl.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class TestServer {
  private final ServerSocket server;
  private int threadCount = 0;

  public TestServer() throws IOException {
    server = new ServerSocket(8123, 0, InetAddress.getByName("localhost"));
  }

  public void start() {
    while (true) {
      try {
        Thread thread = new Thread(new ServerWorker(threadCount++, server.accept()));
        thread.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void close() {
    try {
      server.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
