package com.github.lengl.Authorization;

public class AuthorisationServiceResponse {
  public final User user;
  public final String response;

  public AuthorisationServiceResponse(User user, String response) {
    this.user = user;
    this.response = response;
  }
}
