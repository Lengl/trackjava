package com.github.lengl;

import com.github.lengl.Authorization.AuthorisationClient;
import com.github.lengl.Client.Client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
  public static void main(String[] args) {
    Client client = new Client();
    client.run();
    client.stop();
  }
}
