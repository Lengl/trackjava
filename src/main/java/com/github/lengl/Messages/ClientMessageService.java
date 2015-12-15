package com.github.lengl.Messages;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientMessageService implements InputHandler {

  @Override
  public Message react(@NotNull Message message) {
    String trimmed = message.getBody().trim();

    if (trimmed.startsWith("/")) {
      if (trimmed.startsWith("/chat_send")) {
        return handleChatSend(message);
      }
    }
    return message;
  }

  @Override
  public void stop() {

  }

  @Nullable
  private Message handleChatSend(@NotNull Message message) {
    int OFFSET = 10; //length of
    String idAndMessage = message.getBody().trim().substring(OFFSET).trim();
    if (idAndMessage.matches("\\d+[ ]+.*")) {
      Matcher matcher = Pattern.compile("\\d+").matcher(idAndMessage);
      matcher.find();
      message.setChatId(Integer.valueOf(matcher.group()));

      message.setBody(idAndMessage.replaceFirst("\\d+", "").trim());
      return message;
    } else {
      System.out.println("Usage: /chat_send <chat_id> <message>");
      return null;
    }
  }
}
