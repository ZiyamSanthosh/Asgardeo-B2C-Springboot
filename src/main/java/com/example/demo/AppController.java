package com.example.demo;

import com.example.demo.models.Product;
import com.example.demo.services.ProductService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
class AppController {
    Logger logger = LoggerFactory.getLogger(AppController.class);

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @GetMapping("/index")
    public String getHomePage(Model model, Authentication authentication) {
        List<Product> products = new ProductService().getProducts();

        logger.info("Rendering index page");
        if (authentication == null) {
            model.addAttribute("isAuthenticated", false);
        } else {
            model.addAttribute("isAuthenticated", authentication.isAuthenticated());
            DefaultOidcUser userDetails = (DefaultOidcUser) authentication.getPrincipal();
            model.addAttribute("username", userDetails.getClaim("username"));
            String fullName = "";
            if (null != userDetails.getClaim("given_name")) {
                fullName += userDetails.getClaim("given_name");
            }
            if (null != userDetails.getClaim("family_name")) {
                fullName += " " + userDetails.getClaim("family_name");
            }
            model.addAttribute("fullName", fullName);
        }
        model.addAttribute("productList", products);
        return "index";
    }

    @GetMapping("/")
    public String currentUserName(Model model, Authentication authentication) {

        return "redirect:/index";
    }

    @GetMapping("/login")
    public String getLoginPage(Model model) {

        logger.info("Rendering login page");
        return "redirect:/oauth2/authorization/asgardeo";
    }

    @GetMapping("/profile")
    public String getProfilePage(Model model, Authentication authentication) {

        logger.info("Rendering profile page");
        DefaultOidcUser userDetails = (DefaultOidcUser) authentication.getPrincipal();
        model.addAttribute("username", userDetails.getClaim("username"));
        model.addAttribute("firstName", userDetails.getClaim("given_name"));
        model.addAttribute("lastName", userDetails.getClaim("family_name"));
        model.addAttribute("mobile", userDetails.getClaim("phone_number"));
        if (userDetails.getClaim("address") != null) {
            model.addAttribute("country", new JSONObject(userDetails.getClaim("address").toString()).get("country"));
        }
        return "profile";
    }

    @GetMapping("/updateProfile")
    public String updateProfile(Authentication authentication) throws Exception {

        logger.info("updateProfile");
        System.out.println("updateProfile");
        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
        String clientRegistrationId = authenticationToken.getAuthorizedClientRegistrationId();
        String accessTokenValue = authorizedClientService.loadAuthorizedClient(clientRegistrationId, authentication.getName()).getAccessToken().getTokenValue();
        logger.info("Access token: " + accessTokenValue);
        logger.info("Client registration id: " + clientRegistrationId);
        return "redirect:/profile";
    }

    @RequestMapping({"/add-to-cart", "/product"})
    public String getProductViewPage(Model model, Authentication authentication) {

        logger.info("Rendering product page");
        return "product";
    }
}
