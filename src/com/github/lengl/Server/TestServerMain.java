package com.github.lengl.Server;

import java.io.IOException;

public class TestServerMain {
  public static void main(String[] args) {
    TestServer server = null;
    try {
      server = new TestServer();
      server.start();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (server != null) {
        server.close();
      }
    }
  }
}
