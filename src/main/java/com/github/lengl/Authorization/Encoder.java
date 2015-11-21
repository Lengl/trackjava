package com.github.lengl.Authorization;

import com.sun.istack.internal.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Encoder {
  private static MessageDigest messageDigest = null;
  private static final Logger log = Logger.getLogger(PasswordFileStorage.class.getName());

  static {
    try {
      messageDigest = MessageDigest.getInstance("SHA1");
    } catch (NoSuchAlgorithmException e) {
      log.log(Level.SEVERE, "Encode can't be initialized: ", e);
    }
  }

  @NotNull
  public static String encode(String input) {
    byte[] result = messageDigest.digest(input.getBytes());

    //convert the byte to hex format
    StringBuilder hexString = new StringBuilder();
    for (byte resultByte : result) {
      hexString.append(Integer.toHexString(0xFF & resultByte));
    }
    return hexString.toString();
  }
}
