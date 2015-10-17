package com.github.lengl.Client;

import com.github.lengl.Authorization.AuthorisationClient;
import com.github.lengl.Authorization.User;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Client {
  private static Logger log = Logger.getLogger(Client.class.getName());
  private AuthorisationClient authorisationClient = null;
  private User authorizedUser = null;

  public void run() {
    try {
      LogManager.getLogManager().readConfiguration(
          Client.class.getResourceAsStream("/logging.properties"));
    } catch (IOException e) {
      log.log(Level.SEVERE, "Could not setup logger configuration: ", e);
      System.err.println("Logger mistake. Please restart the client.");
      return;
    }
    try {
      authorisationClient = new AuthorisationClient("passwordStore.mystore");
      authorisationClient.startAuthorizationCycle();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
    } catch (NoSuchAlgorithmException ex) {
      log.log(Level.SEVERE, "NoSuchAlgorithmException: ", ex);
    } finally {
      if (authorisationClient != null) {
        authorisationClient.stopAuthorizationClient();
      }
    }
  }
}
