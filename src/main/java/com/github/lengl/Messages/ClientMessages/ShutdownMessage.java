package com.github.lengl.Messages.ClientMessages;

import com.github.lengl.Messages.ServerMessages.ResponseMessage;

public class ShutdownMessage extends ResponseMessage {
  public ShutdownMessage() {
    super("Server offline. Restart the application");
  }
}
