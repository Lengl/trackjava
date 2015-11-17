package com.github.lengl.Messages.ServerMessages;

import com.sun.istack.internal.NotNull;

public class QuitMessage extends ResponseMessage {
  public QuitMessage(@NotNull String body) {
    super(body);
  }
}
