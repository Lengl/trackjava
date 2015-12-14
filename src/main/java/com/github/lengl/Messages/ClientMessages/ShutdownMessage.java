package com.github.lengl.Messages.ClientMessages;

import com.github.lengl.Messages.Message;

public class ShutdownMessage extends Message {
  public ShutdownMessage() {
    super("Server offline. Restart the application");
  }
}
