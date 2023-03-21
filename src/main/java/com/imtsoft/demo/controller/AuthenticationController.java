package com.imtsoft.demo.controller;


import com.imtsoft.demo.model.AuthenticateRequest;
import com.imtsoft.demo.model.Forget;
import com.imtsoft.demo.model.Register;
import com.imtsoft.demo.model.ResponseObject;
import com.imtsoft.demo.model.Users;
import com.imtsoft.demo.repositories.UserRepository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.imtsoft.demo.service.AuthenticateService;

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
    ) {
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
//    @GetMapping("/test")
//    public Optional<Users> getUser(Users users) {
//    	return userRepository.findByUserName("ram");
//    }

}
