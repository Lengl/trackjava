package com.github.lengl.Authorization;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
  private static Logger log = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    try {
      LogManager.getLogManager().readConfiguration(
          Main.class.getResourceAsStream("/logging.properties"));
    } catch (IOException e) {
      System.err.println("Could not setup logger configuration: " + e.toString());
    }
    try {
      AuthorisationClient authorisationClient = new AuthorisationClient("passwordBase.mybase");
      authorisationClient.startAuthorizationCycle();
      authorisationClient.stopAuthorizationClient();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    } catch (NoSuchAlgorithmException ex) {
      log.log(Level.SEVERE, "NoSuchAlgorithmException: ", ex);
    }
  }
}
