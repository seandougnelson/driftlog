package com.seandougnelson.driftlog;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class DriftlogApplication {

  public static void main(String[] args) {
    SpringApplication driftlogApplication = new SpringApplication(DriftlogApplication.class);
    String secure = getEnv(Env.SECURE);
    if ("true".equalsIgnoreCase(secure)) {
      driftlogApplication.setAdditionalProfiles("secure");
    }
    driftlogApplication.run(args);
  }

  public static String getEnv(Env env) {
    return System.getenv(env.name());
  }

  public static void envNotSet(Logger logger, Env env) {
    logger.error("The environment variable '" + env.name() + "' was not set");
  }

  public static void exit(Logger logger) {
    logger.info("Exiting application...");
    System.exit(1);
  }

}
