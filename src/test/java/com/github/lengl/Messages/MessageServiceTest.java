package com.github.lengl.Messages;

import com.github.lengl.Authorization.AuthorisationService;
import com.github.lengl.Authorization.PasswordStorable;
import com.github.lengl.Users.User;
import com.github.lengl.Users.UserStorable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class MessageServiceTest {
  UserStorable userStore;
  PasswordStorable passwordStore;
  MessageService messageService;
  User defaultUser = new User("Greener", 1);

  @Before
  public void setup() {
    userStore = Mockito.mock(UserStorable.class);
    passwordStore = Mockito.mock(PasswordStorable.class);
    when(userStore.findUserByLogin("Greener")).thenReturn(defaultUser);
    when(userStore.findUserByLogin("Black")).thenReturn(null);
    when(passwordStore.check(defaultUser, "strongpass")).thenReturn(true);
    when(passwordStore.check(defaultUser, "weakpass")).thenReturn(false);
    messageService = new MessageService(new AuthorisationService(userStore, passwordStore));
  }

  @Test
  public void successLogin(){
    Message react = messageService.react("/login Greener strongpass");
    Message expect = new Message("Authorised successfully", "server");
    assertTrue(expect.equals(react));
  }

  @Test
  public void wrongPass() {
    Message expect = new Message("Incorrect password", "server");
    Message react = messageService.react("/login Greener weakpass");
    assertTrue(expect.equals(react));
  }

  @Test
  public void noUser() {
    Message expect = new Message("There is no user with this login.", "server");
    Message react = messageService.react("/login Black mypass");
    assertTrue(expect.equals(react));
  }
  
}