package com.example.demo;

import com.example.demo.exceptions.LoginException;
import com.example.demo.models.Product;
import com.example.demo.services.ProductService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Controller
class AppController {
    Logger logger = LoggerFactory.getLogger(AppController.class);

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private ProductService productService;


    @Value("${asgardeo.scim.me.endpoint}")
    private String scimMeEndpoint;


    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/index")
    public String getHomePage(Model model, Authentication authentication) {
        List<Product> products = productService.getProducts();

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
            String accessToken = getAccessToken((OAuth2AuthenticationToken) authentication);
            if (accessToken == null) {
                return "redirect:/login";
            }
            boolean emailVerified = false;
            try {
                emailVerified = getEmailVerifiedClaim(authentication, accessToken);
            } catch (LoginException e) {
                return "redirect:/login";
            }
            logger.info("Email verified: " + emailVerified);
            model.addAttribute("emailVerified", emailVerified);
            try {
                model.addAttribute("tier", getUserTier(authentication, accessToken));
            } catch (LoginException e) {
                return "redirect:/login";
            }

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

        String accessToken = getAccessToken((OAuth2AuthenticationToken) authentication);
        return "redirect:/profile";
    }

    @RequestMapping({"/add-to-cart", "/product"})
    public String getProductViewPage(Model model, Authentication authentication) {

        int statusCode = productService.addProduct(getAccessToken((OAuth2AuthenticationToken) authentication));
        if (statusCode == 200) {
            logger.info("Rendering product page");
            return "redirect:/index";
        } else {
            return "redirect:/error";
        }
    }

    private String getAccessToken(OAuth2AuthenticationToken authentication) {

        String clientRegistrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        logger.info(clientRegistrationId);
        OAuth2AuthorizedClient oauthclient = authorizedClientService.loadAuthorizedClient(clientRegistrationId, authentication.getName());
        if (oauthclient == null) {
            return null;
        }
        OAuth2AccessToken accessToken = oauthclient.getAccessToken();
        if (accessToken == null) {
            return null;
        } else {
            logger.info("Access token:" + accessToken.getTokenValue());
            return accessToken.getTokenValue();
        }
    }

    private boolean getEmailVerifiedClaim(Authentication authentication, String accessToken) throws LoginException {

        logger.info("getEmailVerifiedClaim");
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        JSONObject response = callGetAPI(scimMeEndpoint, entity);
        JSONObject customSchemaAttributes = (JSONObject)response.get("urn:scim:wso2:schema");
        logger.info(customSchemaAttributes.toString());
        if (!customSchemaAttributes.has("emailVerified") || customSchemaAttributes.get("emailVerified") == null) {
            return false;
        }
        String emailVerified = (String) customSchemaAttributes.get("emailVerified");
        return Boolean.parseBoolean(emailVerified);
    }

    private String getUserTier(Authentication authentication, String accessToken) throws LoginException {

        logger.info("GetUserTier");
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        JSONObject response = callGetAPI(scimMeEndpoint, entity);
        JSONObject customSchemaAttributes = (JSONObject)response.get("urn:scim:wso2:schema");
        logger.info(customSchemaAttributes.toString());
        if (!customSchemaAttributes.has("tier") || customSchemaAttributes.get("tier") == null) {
            return "";
        }
        return (String) customSchemaAttributes.get("tier");
    }

    private JSONObject callGetAPI(String url, HttpEntity<String> entity) throws LoginException {

        logger.info("get api");
        ResponseEntity<String> restApi = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        int statusCode = restApi.getStatusCode().value();
        logger.info(statusCode + "");
        if (statusCode == 200) {
            String response = restApi.getBody();
            logger.info(response);
            return new JSONObject(response);
        } else {
            logger.info("Error occurred while calling the API");
            throw new LoginException("Error occurred while calling the API");
        }
    }
}
