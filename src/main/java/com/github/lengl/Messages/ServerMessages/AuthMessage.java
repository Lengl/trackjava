package com.github.lengl.Messages.ServerMessages;

import com.github.lengl.Users.User;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;


public class AuthMessage extends ResponseMessage {
  private transient User authorized;

  public AuthMessage(@NotNull String body, @Nullable User authorized) {
    super(body);
    this.authorized = authorized;
  }

  public User getAuthorized() {
    return authorized;
  }
}
