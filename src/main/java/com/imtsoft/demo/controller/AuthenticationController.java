package com.imtsoft.demo.controller;


import com.imtsoft.demo.model.AuthenticateRequest;
import com.imtsoft.demo.model.Forget;
import com.imtsoft.demo.model.Register;
import com.imtsoft.demo.model.ResponseObject;
import com.imtsoft.demo.model.Users;
import com.imtsoft.demo.repositories.UserRepository;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.imtsoft.demo.service.AuthenticateService;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    @Autowired
    private final AuthenticateService authenticateService;
    @Autowired
    private  UserRepository userRepository;

    public AuthenticationController(AuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> register(
        @RequestBody Register request
    ) throws MessagingException, UnsupportedEncodingException {
        return ResponseEntity.ok(authenticateService.register(request));
    }
    @PostMapping("/authenticate")
    public ResponseEntity<ResponseObject> authenticate(
            @RequestBody AuthenticateRequest request
    ) {
        return ResponseEntity.ok(authenticateService.authenticate(request));
    }
    
    @PostMapping("/authenticate/forget")
    public ResponseEntity<ResponseObject> forgetPass(
            @RequestBody Forget request
    ) {
        return ResponseEntity.ok(authenticateService.forget(request));
    }
    @PostMapping("/confirm/{id}")
    public ResponseEntity<ResponseObject> confirmEmail(
            @RequestBody String token, @PathVariable Integer id
    ) {
        return ResponseEntity.ok(authenticateService.confirmEmail(token, id));
    }
//    @GetMapping("/test")
//    public Optional<Users> getUser(Users users) {
//    	return userRepository.findByUserName("ram");
//    }

}
