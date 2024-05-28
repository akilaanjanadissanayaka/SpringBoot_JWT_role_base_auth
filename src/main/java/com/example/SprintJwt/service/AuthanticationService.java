package com.example.SprintJwt.service;


import com.example.SprintJwt.model.AuthenticationResponse;
import com.example.SprintJwt.model.Token;
import com.example.SprintJwt.model.User;
import com.example.SprintJwt.repository.TokenRipository;
import com.example.SprintJwt.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthanticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private  final TokenRipository tokenRipository;
    public AuthanticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, TokenRipository tokenRipository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.tokenRipository = tokenRipository;
    }
    public AuthenticationResponse register(User request) {



        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));


        user.setRole(request.getRole());

        user = repository.save(user);

        String token = jwtService.generateToken(user);

        revokeAllTokenByUser(user);
        saveUserToken(token, user);


        return new AuthenticationResponse(token);

    }

    public AuthenticationResponse authenticate(User request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = repository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.generateToken(user);

        revokeAllTokenByUser(user);
        saveUserToken(token, user);

        return new AuthenticationResponse(token);

    }

    private void saveUserToken(String jwt, User user) {
        Token token=new Token();
        token.setToken(jwt);
        token.setUser(user);
        token.setLoggedOut(false);
        tokenRipository.save(token);
    }
    private void revokeAllTokenByUser(User user) {
        List<Token> validTokens = tokenRipository.findAllTokenByUser(user.getId());
        if(validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t-> {
            t.setLoggedOut(true);
        });

        tokenRipository.saveAll(validTokens);
    }
}
