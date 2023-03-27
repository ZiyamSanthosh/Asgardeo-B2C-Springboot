package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class ConfigSecurity extends WebSecurityConfigurerAdapter {

    Logger logger = LoggerFactory.getLogger(ConfigSecurity.class);

    protected void configure(HttpSecurity http) throws Exception {

        logger.info("Configuring security");
        http.authorizeRequests()
                .antMatchers( "/index", "/login")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2Login().loginPage("/login");
    }
}
