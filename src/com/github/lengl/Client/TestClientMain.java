package com.github.lengl.Client;

import java.io.IOException;

public class TestClientMain {
  public static void main(String[] args) {
    TestClient client = null;
    try {
      client = new TestClient();
      client.start();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (client != null) {
        client.close();
      }
    }
  }
}
