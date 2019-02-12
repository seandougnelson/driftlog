package com.seandougnelson.driftlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class DriftlogApplication {

  private static Logger logger = LoggerFactory.getLogger(DriftlogApplication.class);

  public static void main(String[] args) {
    SpringApplication driftlogApplication = new SpringApplication(DriftlogApplication.class);
    List<String> profiles = new ArrayList<>();
    List<Env> requiredEnvs = new ArrayList<>();

    String secure = Env.SECURE.getValue();
    if ("true".equalsIgnoreCase(secure)) {
      profiles.add("secure");
      Collections.addAll(requiredEnvs, Env.DRIFTLOG_USER, Env.DRIFTLOG_PASSWORD);
    }
    String https = Env.HTTPS.getValue();
    if ("true".equalsIgnoreCase(https)) {
      profiles.add("https");
      Collections.addAll(requiredEnvs, Env.SSL_KEY_ALIAS, Env.SSL_KEYSTORE_PATH, Env.SSL_KEYSTORE_TYPE,
              Env.SSL_KEYSTORE_PASSWORD);
    }
    requiredEnvs.add(Env.ALLOWED_LOG_DIRS);

    boolean exitApplication = false;
    for (Env requiredEnv : requiredEnvs) {
      if (requiredEnv.getValue() == null) {
        logger.error("The environment variable '" + requiredEnv.name() + "' was not set");
        exitApplication = true;
      }
    }
    if (exitApplication) {
      exit();
    }
    if (!profiles.isEmpty()) {
      driftlogApplication.setAdditionalProfiles(profiles.toArray(String[]::new));
    }

    driftlogApplication.run(args);
  }

  public static void exit() {
    logger.info("Exiting application...");
    System.exit(1);
  }

}
