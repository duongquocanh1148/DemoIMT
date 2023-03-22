package com.imtsoft.demo.service;


import com.imtsoft.demo.model.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.imtsoft.demo.repositories.TokenRepository;
import com.imtsoft.demo.repositories.UserRepository;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthenticateService {
	
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
   
    private final JwtService jwtService;
    
    private final TokenRepository tokenRepository;
    
    private final GmailService gmailService;

    private String jwt;
    private final AuthenticationManager authenticationManager;
    public ResponseObject register(Register request) throws MessagingException, UnsupportedEncodingException {

        if(validateEmail(request.getEmail()) && validateUserName(request.getUserName()) && validatePassword(request.getPassword())){
            var user = Users.builder()
                    .userName(request.getUserName())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getEmail())
                    .doB(request.getDoB())
                    .isConfirm(false)
                    .build();
            userRepository.save(user);
            jwt = jwtService.generateToken(user);
            gmailService.SendMail(user.getEmail(), jwt);
            return ResponseObject.builder()
                    .status("OK")
                    .message("token: " + jwt)
                    .data(user)
                    .build();
        }
        return ResponseObject.builder()
                .status("Failed")
                .message("Please check your information input!")
                .build();
        }
    public ResponseObject confirmEmail(String token, Integer id){
        if(token.equals(jwt)){
            Optional<Users> update = userRepository.findById(id).map(users -> {
                users.setConfirm(true);
                return userRepository.save(users);
            });
            return ResponseObject.builder()
                    .status("OK")
                    .message("Confirm successfully")
                    .build();
        }
        return ResponseObject.builder()
                .status("Failed")
                .message("Please check your token again!")
                .build();
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
       Users user = userRepository.findByUserName(request.getUserName())
                .orElse(null);
       if (user!=null) {
    	   jwt = jwtService.generateToken(user);
           saveUserToken(user, jwt);
           return ResponseObject.builder()
           		.status("OK")
           		.message("Login successfully")
                   .data(jwt)
                   .build();
	} else {
		return ResponseObject.builder()
           		.status("Failed")
           		.message("Login fail")
                   .build();
	}
        
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
    
    public ResponseObject forget(Forget request) throws UnsupportedEncodingException, MessagingException{
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getEmail(),
//                        null
//                )
//        );
        Users  user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        if (user!=null) {
        	jwt = jwtService.generateToken(user);
            saveUserToken(user, jwt);
            gmailService.SendMail(user.getEmail(), jwt);
            return ResponseObject.builder()
            		.status("OK")
            		.message("please check your email to get token")
                    .data(jwt)
                    .build();
		}
        else {
        	return ResponseObject.builder()
        			.status("Failed")
                    .message("Please check your email again!")
                    .build();
		}
    }
    
    public ResponseObject checkTokenSendToEmail(ResetPassword resetPassword,String email){
        if(resetPassword.getToken().equals(jwt)){
            Optional<Users> update = userRepository.findByEmail(email).map(users -> {
                users.setPassword(passwordEncoder.encode(resetPassword.getPassword()));
                return userRepository.save(users);
            });
        	
            return ResponseObject.builder()
                    .status("OK")
                    .message("Change password successfully")
                    .build();
        }
        return ResponseObject.builder()
                .status("Failed")
                .message("Please check your token again!")
                .build();
    }


}
