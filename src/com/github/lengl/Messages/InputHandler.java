package com.github.lengl.Messages;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

public interface InputHandler {
  @Nullable
  String react(@NotNull String input);

  @Nullable
  String getAuthor();
}
