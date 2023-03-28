package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@EnableWebSecurity
public class ConfigSecurity extends WebSecurityConfigurerAdapter {

    @Value( "${spring.security.oauth2.client.registration.asgardeo.post-logout-redirect-uri}" )
    private String postLogoutRedirectUri;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    Logger logger = LoggerFactory.getLogger(ConfigSecurity.class);

    protected void configure(HttpSecurity http) throws Exception {

        logger.info("Configuring security");
        http.authorizeRequests()
                .antMatchers( "/index", "/product", "/login", "/")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2Login().loginPage("/login")
                .and().logout()
                .logoutSuccessHandler(oidcLogoutSuccessHandler());

    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(
                        this.clientRegistrationRepository);

        oidcLogoutSuccessHandler.setPostLogoutRedirectUri(postLogoutRedirectUri);
        return oidcLogoutSuccessHandler;
    }
}
