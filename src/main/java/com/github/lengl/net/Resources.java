package com.github.lengl.net;

import com.github.lengl.Authorization.AuthorisationService;
import com.github.lengl.Authorization.PasswordDBStorage;
import com.github.lengl.ChatRoom.ChatRoomDBStorage;
import com.github.lengl.ChatRoom.ChatRoomStorable;
import com.github.lengl.Messages.MessageDBStorage;
import com.github.lengl.Messages.MessageStorable;
import com.github.lengl.Users.UserDBStorage;
import com.github.lengl.jdbc.QueryExecutor;

public class Resources {
  public final QueryExecutor queryExecutor;
  public final AuthorisationService authorisationService;
  public final MessageStorable historyStorage;
  public final ChatRoomStorable chatRoomStorage;

  public Resources() throws Exception {
    queryExecutor = new QueryExecutor();
    authorisationService = new AuthorisationService(new UserDBStorage(queryExecutor), new PasswordDBStorage(queryExecutor));
    historyStorage = new MessageDBStorage(queryExecutor);
    chatRoomStorage = new ChatRoomDBStorage(queryExecutor);
  }

  public void close() {
    chatRoomStorage.close();
    historyStorage.close();
    authorisationService.stop();
    queryExecutor.close();
  }
}
