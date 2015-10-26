package com.github.lengl.Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TestClient {
  private final Socket socket;
  private final BufferedReader socketReader;
  private final BufferedWriter socketWriter;
  private final BufferedReader consoleReader;
  private final BufferedWriter consoleWriter;

  public TestClient() throws IOException {
    socket = new Socket("localhost", 8123);
    socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    consoleReader = new BufferedReader(new InputStreamReader(System.in));
    consoleWriter = new BufferedWriter(new OutputStreamWriter(System.out));
  }

  public void start() {
    String output;
    StringBuilder builder = new StringBuilder();
    while(socket.isConnected()) {
      try {
        socketWriter.write(consoleReader.readLine());
        socketWriter.newLine();
        socketWriter.flush();
        while ((output = socketReader.readLine()) != null) {
          builder.append(output);
        }
        output = builder.toString();

        consoleWriter.write("response:" + output);
        consoleWriter.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void close() {
    try {
      consoleWriter.close();
      consoleReader.close();
      socketWriter.close();
      socketReader.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
