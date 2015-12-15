package com.github.lengl.Messages.ServerMessages;

import com.github.lengl.Messages.Message;
import com.sun.istack.internal.NotNull;

public class ResponseMessage extends Message {
  public ResponseMessage(@NotNull String body) {
    super(body);
    this.setAuthor("server");
  }
}
