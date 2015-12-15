package com.github.lengl.integr;

import com.github.lengl.Messages.Message;
import com.github.lengl.Messages.ServerMessageService;
import com.github.lengl.Messages.ServerMessages.ResponseMessage;
import com.github.lengl.net.MessageListener;
import com.github.lengl.net.ThreadedClient;
import com.github.lengl.net.ThreadedServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    try {
      Thread.sleep(60000);
    } catch (InterruptedException ignored) {
      System.out.println("Connected");
    }
    client.getHandler().addListener(this);
  }

  @Test
  public void noLoginMessage() throws Exception {
    boolean temp = gotResult(new ResponseMessage("To send messages you have to be authorized! (/login <user> <password>)"), "Somestr");
    assertTrue(temp);
  }

  @Test
  @Ignore
  public void wrongLoginPass() throws Exception {
    assertTrue(gotResult(new ResponseMessage("Incorrect password"), "/login lengl hell"));
    //assertTrue(gotResult(new ResponseMessage("There is no user with this login."), "/login leng hell"));
  }

  @Test
  public void correctPass() throws Exception {
    assertTrue(gotResult(new ResponseMessage("Authorised successfully\nUser{id=4, login='lengl', nickname='lengl'}"), "/login lengl hello"));
  }

  @Test
  public void defaultChatSend() throws Exception {
    correctPass();
    assertTrue(gotResult(new Message("Something", "lengl"), "Something"));
  }

  @Test
  public void chatCreate() throws Exception {
    correctPass();
    client.processInput("/chat_create 5");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ignored) {}
    assertTrue(result.matches("Author=<server>, Message=<Chat \\d+ created successfully.>"));
  }


  @Override
  public void onMessage(Message message) {
    result = message.toString();
  }

  private boolean gotResult(Object result, String on) throws Exception {
    client.processInput(on);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ignored) {}
    return this.result.equals(result.toString());
  }

  @After
  public void close() {
    client.close();
  }
}
