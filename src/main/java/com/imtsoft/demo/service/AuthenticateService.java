package com.imtsoft.demo.service;


import com.imtsoft.demo.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.imtsoft.demo.repositories.TokenRepository;
import com.imtsoft.demo.repositories.UserRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                .password(request.getPassword())
                .email(request.getEmail())
                .doB(request.getDoB())
                .build();
        if(validateEmail(user.getEmail()) && validateUserName(user.getUsername()) && validatePassword(user.getPassword())){
            userRepository.save(user);
            var jwt = jwtService.generateToken(user);
            return ResponseObject.builder()
                    .status("OK")
                    .message("token: " + jwt)
                    .data(user)
                    .build();
        }
        return ResponseObject.builder()
                .status("Failed")
                .message("Please check your information input!")
                .data(user).build();
        }
    boolean validateEmail(String value) {
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
    boolean validateUserName(String value){
        String regexPattern = "^[a-z0-9].{8,32}$";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
    boolean validatePassword(String value){
        String regexPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!.#$@_+,?-]).{8,32}$";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
    public ResponseObject authenticate(AuthenticateRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserName(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByUserName(request.getUserName())
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
    
    public ResponseObject forget(Forget request){
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getEmail(),
//                        request.getPassword()
//                )
//        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwt = jwtService.generateToken(user);
        saveUserToken(user, jwt);
        return ResponseObject.builder()
                .data(jwt)
                .build();
    }
}
