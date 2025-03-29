package org.tokenApi.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.access.Role;
import org.access.RoleManager;
import org.access.Subscription;
import org.apiUtils.commonClasses.TokenAuthController;
import org.company.entities.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tokens.entities.AppToken;
import org.tokens.entities.TokenUserLink;
import org.tokens.repositories.TokenRepository;
import org.tokens.repositories.TokenUserLinkRepository;
import org.springframework.validation.annotation.Validated;
import org.tokenApi.exceptions.TokenExceptions;
import org.user.entities.AppUser;
import org.user.repositories.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
@RestController
@Validated
public class TokenController extends TokenAuthController {

    private final TokenRepository tokenRepo;

    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder encoder;

    @Autowired
    public TokenController(
                        UserRepository userRepository,
                        TokenUserLinkRepository tokenUserLinkRepository,
                        TokenRepository tokenRepository) {

        super(userRepository, tokenUserLinkRepository);
        this.tokenRepo = tokenRepository;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.encoder = new BCryptPasswordEncoder();

    }

    /**
     * Validates if the current user has higher priority than the requested role
     * @param currentUser The current authenticated user
     * @param lowerRole The role to compare against
     * @throws TokenExceptions.InsufficientRoleAuthority If the current user's role does not have higher priority
     */
    private void validateRoleAuthority(AppUser currentUser, Role lowerRole) {
        // Check if current user's role has higher priority than the requested role
        if (!currentUser.getRole().isHigherPriorityThan(lowerRole)) {
            throw new TokenExceptions.InsufficientRoleAuthority(
                "Cannot generate token for role with equal or higher priority");
        }
    }

    private void checkTokenLimit(Company company, Role role) {
        Subscription subscription = company.getSubscription();
        
        List<AppToken> activeTokens = tokenRepo.findByCompanyAndRole(
            company, role);
        
        if (activeTokens.size() >= subscription.getMaxUsers(role)) {
            throw new TokenExceptions.NumTokensLimitExceeded(
                "Maximum number of active tokens reached for role: " + role);
        }
    }

    // can be annotated as @Transactional, but might slow down the performance significantly.
    private String generateCompanyUniqueToken(Company company) {
        // extract the company tokens from the database
        List<AppToken> companyTokens = tokenRepo.findByCompany(company); 

        boolean match = true;

        String tokenValue = null;
        while (match) {
            match = false;
            tokenValue = UUID.randomUUID().toString();
            // iterate over the company tokens and check if the token is unique
            for (AppToken token : companyTokens) {
                if (encoder.matches(tokenValue, token.getTokenHash())) {
                    match = true;
                    break;
                }
            }
        }
        
        // at this point of the code we know that the token is unique
        return tokenValue;
    }

    private AppToken saveToken(String tokenValue, Company company, Role requestedRole) {
        // create the new token
        String tokenId = UUID.randomUUID().toString(); 

        while (tokenRepo.findById(tokenId).isPresent()) {
            tokenId = UUID.randomUUID().toString();
        }

        AppToken token = new AppToken(tokenId, tokenValue, company, requestedRole);
        tokenRepo.save(token);

        return token;
    }

    /**
     * Generates a new token for the specified role
     * @param role The role for which to create the token
     * @param userDetails Current authenticated user
     * @return Response with the generated token
     * @throws JsonProcessingException If error occurs during JSON serialization
     */
    @GetMapping("/api/token/generate")
    public ResponseEntity<String> generateToken(
            @RequestParam String role, 
            @AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {
        
        // Get the current user
        AppUser currentUser = authorizeUserToken(userDetails);
        Company company = currentUser.getCompany();
        Role requestedRole = RoleManager.getRole(role);
            
        // validate the role authority
        validateRoleAuthority(currentUser, requestedRole);

        // make sure limits are not exceeded
        checkTokenLimit(company, requestedRole);

        // generate a unique token for the company
        String tokenValue = generateCompanyUniqueToken(company);
        
        AppToken token = saveToken(tokenValue, company, requestedRole);
        this.tokenRepo.save(token);

        // Create response with token value
        Map<String, String> response = new HashMap<>();
        response.put("token", tokenValue);
        
        return ResponseEntity.ok(objectMapper.writeValueAsString(response));
    }

    private TokenUserLink validateUserTokenLink(AppUser targetUser) {
        // make sure the user is linked to a token
        Optional<TokenUserLink> tokenLink = this.tokenUserLinkRepo.findFirstByUser(targetUser);
        
        if (tokenLink.isEmpty()) {
            throw new TokenExceptions.NoUserTokenLinkException(
                "No active token found for user: " + targetUser.getEmail());
        }
        
        // make sure the token is active
        if (this.tokenRepo.findById(tokenLink.get().getToken().getTokenId()).isEmpty()) {
            throw new TokenExceptions.NoActiveTokenException(
                "Token not found: " + tokenLink.get().getToken().getTokenId());
        }

        return tokenLink.get();
    }

    /**
     * Revokes a token associated with a user
     * @param userEmail Email of the user whose token should be revoked (optional)
     * @param userDetails Current authenticated user
     * @return Success response
     * @throws JsonProcessingException If error occurs during JSON serialization
     */
    @GetMapping("/api/token/revoke")
    public ResponseEntity<String> revokeToken(
            @RequestParam String userEmail, 
            @AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {
        
        // make sure the current has his token situation sorted out.
        authorizeUserToken(userDetails);
            
        // make sure the user exists
        AppUser targetUser = this.userRepo.findById(userEmail).orElseThrow(() -> new TokenExceptions.RevokedUserNotFoundException("User not found: " + userEmail));

        // make sure the user is linked to a token
        TokenUserLink tokenLink = validateUserTokenLink(targetUser);

        // make sure to delete the token link and the token 
        AppToken token = tokenLink.getToken();
        this.tokenUserLinkRepo.delete(tokenLink);
        this.tokenRepo.delete(token);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Token(s) successfully revoked for user: " + targetUser.getEmail());
        
        return ResponseEntity.ok(objectMapper.writeValueAsString(response));
    }

    /**
     * Retrieves all tokens matching the specified role criteria
     * @param role Role to filter tokens by (optional)
     * @param userDetails Current authenticated user
     * @return List of tokens
     * @throws JsonProcessingException If error occurs during JSON serialization
     */
    @GetMapping("/api/token/all")
    public ResponseEntity<String> getAllTokens(
            @RequestParam(required = false) String role,
            @AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {
        
        // Get the current user
        AppUser currentUser = authorizeUserToken(userDetails);
        Company company = currentUser.getCompany();
        Role currentRole = currentUser.getRole();
        
        List<Role> lowerPriorityRoles = new ArrayList<>(); 
        
        if (role == null) {
            for (Role r : RoleManager.ROLES) {
                if (currentRole.isHigherPriorityThan(r)) {
                    lowerPriorityRoles.add(r);
                }
            }    
        }
        else { 
            Role requestedRole = RoleManager.getRole(role);
            lowerPriorityRoles.add(requestedRole);
        }
        
        // make sure the roles are of lower priority than the current role 
        for (Role r : lowerPriorityRoles) {
            validateRoleAuthority(currentUser, r);
        }
        
        List<AppToken> resultTokens = new ArrayList<>();
        for (Role r : lowerPriorityRoles) {
            resultTokens.addAll(tokenRepo.findByCompanyAndRole(company, r));
        }
    
        return ResponseEntity.ok(objectMapper.writeValueAsString(resultTokens));
    }
}
