package com.github.lengl.Messages;

import com.github.lengl.Authorization.AuthorisationService;
import com.github.lengl.Authorization.PasswordStorable;
import com.github.lengl.Messages.ServerMessages.AuthMessage;
import com.github.lengl.Users.User;
import com.github.lengl.Users.UserStorable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ServerMessageServiceTest {
  //This one is out of date
/*  UserStorable userStore;
  PasswordStorable passwordStore;
  ServerMessageService serverMessageService;
  User defaultUser = new User("Greener", 1);

  @Before
  public void setup() throws Exception {
    userStore = Mockito.mock(UserStorable.class);
    passwordStore = Mockito.mock(PasswordStorable.class);
    when(userStore.findUserByLogin("Greener")).thenReturn(defaultUser);
    when(userStore.findUserByLogin("Black")).thenReturn(null);
    when(passwordStore.check(defaultUser, "strongpass")).thenReturn(true);
    when(passwordStore.check(defaultUser, "weakpass")).thenReturn(false);
    serverMessageService = new ServerMessageService(new AuthorisationService(userStore, passwordStore));
  }

  @Test
  public void successLogin() {
    Message react = serverMessageService.react(new Message("/login Greener strongpass"));
    Message expect = new AuthMessage("Authorised successfully\n" + defaultUser.toString(), defaultUser);
    assertTrue(expect.equals(react));
  }

  @Test
  public void wrongPass() {
    Message expect = new AuthMessage("Incorrect password", null);
    Message react = serverMessageService.react(new Message("/login Greener weakpass"));
    assertTrue(expect.equals(react));
  }

  @Test
  public void noUser() {
    Message expect = new AuthMessage("There is no user with this login.", null);
    Message react = serverMessageService.react(new Message("/login Black mypass"));
    assertTrue(expect.equals(react));
  }*/
  
}