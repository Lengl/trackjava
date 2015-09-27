package com.github.lengl.Authorization;

import java.io.IOException;

public class Main {

  public static void main(String[] args) {
    try {
      AuthorisationClient authorisationClient = new AuthorisationClient();
      authorisationClient.startAuthorizationCycle();
    } catch (IOException e) {
      //log it
    }
  }
}
