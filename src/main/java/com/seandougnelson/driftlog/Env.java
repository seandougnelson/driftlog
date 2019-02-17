package com.seandougnelson.driftlog;

public enum Env {
  SECURE, HTTPS, SSL_KEY_ALIAS, SSL_KEYSTORE_PATH, SSL_KEYSTORE_TYPE, SSL_KEYSTORE_PASSWORD, DRIFTLOG_USER,
  DRIFTLOG_PASSWORD, ALLOWED_LOG_DIRS, ALLOWED_LOG_EXTENSIONS;

  public String getValue() {
    String value = System.getenv(this.name());
    if (value == null) {
      value = System.getProperty(this.name());
    }
    return value;
  }

}
