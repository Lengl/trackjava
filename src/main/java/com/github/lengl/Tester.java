package com.github.lengl;


import com.github.lengl.Messages.Message;

public class Tester {

  public static void main(String[] args) {
    Message msg = new Message("hello");
    msg.setChatId(-1);
    System.out.println(msg.getChatId() == -1);
  }
}
