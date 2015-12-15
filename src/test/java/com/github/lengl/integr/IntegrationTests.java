package com.github.lengl.integr;

import com.github.lengl.Messages.Message;
import com.github.lengl.Messages.ServerMessageService;
import com.github.lengl.Messages.ServerMessages.ResponseMessage;
import com.github.lengl.net.MessageListener;
import com.github.lengl.net.ThreadedClient;
import com.github.lengl.net.ThreadedServer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntegrationTests implements MessageListener {
  private ThreadedClient client;
  private String result;

  @Before
  public void setup() throws Exception {
    new Thread(() -> {
      ThreadedServer.main(null);
    }).start();
    Thread.sleep(100);
    client = new ThreadedClient();
    client.getHandler().addListener(this);
  }

  @Test
  public void noLoginMessage() throws Exception {
    boolean temp = gotResult(new ResponseMessage(ServerMessageService.UNAUTHORIZED), "Somestr");
    assertTrue(temp);
  }

  @Test
  public void wrongLoginPass() throws Exception {
    assertTrue(gotResult(new ResponseMessage("Incorrect password"), "/login lengl hell"));
    assertTrue(gotResult(new ResponseMessage("There is no user with this login."), "/login leng hell"));
  }

  @Test
  public void correctPass() throws Exception {
    assertTrue(gotResult(new ResponseMessage("Authorised successfully"), "/login lengl hello"));
  }

  @Test
  public void defaultChatSend() throws Exception {
    assertTrue(gotResult(new Message("Something"), "Something"));
  }


  @Override
  public void onMessage(Message message) {
    result = message.toString();
  }

  private boolean gotResult(Object result, String on) throws Exception {
    client.processInput(on);
    return this.result.equals(result.toString());
  }
}
