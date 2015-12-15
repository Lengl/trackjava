package com.github.lengl.Messages;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

public interface InputHandler {
  @Nullable
  Message react(@NotNull Message message);

  void stop();
}
