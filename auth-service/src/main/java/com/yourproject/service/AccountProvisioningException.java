package com.yourproject.service;

public class AccountProvisioningException extends RuntimeException {
  public AccountProvisioningException(String message, Throwable cause) {
    super(message, cause);
  }
}
