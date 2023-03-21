package com.imtsoft.demo.service;


import com.imtsoft.demo.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.imtsoft.demo.repositories.TokenRepository;
import com.imtsoft.demo.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthenticateService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    private final AuthenticationManager authenticationManager;
    public ResponseObject register(Register request){
        var user = Users.builder()
                .userName(request.getUserName())
                .password((passwordEncoder.encode(request.getPassword())))
                .email(request.getEmail())
                .doB(request.getDoB())
                .build();
        userRepository.save(user);
        var jwt = jwtService.generateToken(user);
        return ResponseObject.builder()
                .status("")
                .message("")
                .data(jwt)
                .build();
    }

    public ResponseObject authenticate(AuthenticateRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwt = jwtService.generateToken(user);
        saveUserToken(user, jwt);
        return ResponseObject.builder()
                .data(jwt)
                .build();
    }

    private void saveUserToken(Users user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType("BEARER")
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
    private void revokeAllUserTokens(Users user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
}
