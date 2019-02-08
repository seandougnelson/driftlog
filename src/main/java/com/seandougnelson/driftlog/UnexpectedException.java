package com.seandougnelson.driftlog;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UnexpectedException extends ResponseStatusException {

  public UnexpectedException(Throwable e) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected exception occurred (view 'trace' for additional info)", e);
  }

}
