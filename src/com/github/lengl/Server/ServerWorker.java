package com.github.lengl.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ServerWorker implements Runnable{
  private final int id;
  private final Socket socket;
  private final BufferedReader reader;
  private final BufferedWriter writer;

  public ServerWorker(int id, Socket socket) throws IOException {
    this.id = id;
    this.socket = socket;
    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
  }

  @Override
  public void run() {
    StringBuilder builder = new StringBuilder();
    String input;
    //infinite loop
    while (socket.isConnected()) {
      try {
        while ((input = reader.readLine()) != null) {
          builder.append(input);
        }
        input = builder.toString();

        System.out.println(id + ":" + input);

        writer.write(input);
        writer.newLine();
        writer.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
