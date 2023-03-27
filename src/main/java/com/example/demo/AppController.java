package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class AppController {

    Logger logger = LoggerFactory.getLogger(AppController.class);

    @GetMapping("/index")
    public String getProfile(Model model, Authentication authentication) {

        logger.info("index");
        return "index";
    }

    @GetMapping("/")
    public String currentUserName(Model model, Authentication authentication) {

        DefaultOidcUser userDetails = (DefaultOidcUser) authentication.getPrincipal();
        model.addAttribute("userName", userDetails.getName());
        model.addAttribute("IDTokenClaims", userDetails);
        return "home";
    }


    @GetMapping("/login")
    public String getLoginPage(Model model) {

        logger.info("login");
        return "redirect:/oauth2/authorization/asgardeo";
    }
}
