package com.seandougnelson.driftlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@Profile("secure")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  private Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);
  private PasswordEncoder passwordEncoder;
  private String username;
  private String password;

  public SecurityConfiguration() {
    boolean exitApplication = false;

    username = DriftlogApplication.getEnv(Env.DRIFTLOG_USER);
    if (username == null) {
      DriftlogApplication.envNotSet(logger, Env.DRIFTLOG_USER);
      exitApplication = true;
    }

    password = DriftlogApplication.getEnv(Env.DRIFTLOG_PASSWORD);
    if (password == null) {
      DriftlogApplication.envNotSet(logger, Env.DRIFTLOG_PASSWORD);
      exitApplication = true;
    }

    if (exitApplication) {
      DriftlogApplication.exit(logger);
    }

    passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
    authenticationManagerBuilder
            .inMemoryAuthentication()
            .withUser(username)
            .password(passwordEncoder.encode(password))
            .roles("USER");
  }

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
            .authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .httpBasic();
  }
}
