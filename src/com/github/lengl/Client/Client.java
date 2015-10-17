package com.github.lengl.Client;

import com.github.lengl.Authorization.AuthorisationClient;
import com.github.lengl.Authorization.User;
import com.github.lengl.Messages.MessageService;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Client {
  private static Logger log = Logger.getLogger(Client.class.getName());
  private AuthorisationClient authorisationClient = null;
  private User authorizedUser = null;
  private MessageService messageService = null;

  public void run() {
    try {
      LogManager.getLogManager().readConfiguration(
          Client.class.getResourceAsStream("/logging.properties"));
    } catch (IOException e) {
      log.log(Level.SEVERE, "Could not setup logger configuration: ", e);
      System.err.println("Logger error. Please restart the client.");
      return;
    }
    log.info("Client started");

    try {
      //The main part
      //authorisation
      authorisationClient = new AuthorisationClient("passwordStore.mystore");
      authorizedUser = authorisationClient.startAuthorizationCycle();
      //message exchange
      if (authorizedUser != null) {
        messageService = new MessageService(authorizedUser);
        messageService.run();
      }
    } catch (IOException e) {
      log.log(Level.SEVERE, "IOException: ", e);
      System.err.println("I/O error. Please restart the client.");
    } catch (NoSuchAlgorithmException ex) {
      log.log(Level.SEVERE, "NoSuchAlgorithmException: ", ex);
      System.err.println("General error. Please restart the client.");
    }
  }

  public void stop() {
    authorizedUser = null;
    if (authorisationClient != null) {
      authorisationClient.stopAuthorizationClient();
    }
    if (messageService != null) {
      messageService.stop();
    }
    log.info("Client stopped");
  }
}
