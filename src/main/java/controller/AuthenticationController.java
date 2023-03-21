package controller;


import model.AuthenticateRequest;
import model.Register;
import model.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.AuthenticateService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    @Autowired
    private final AuthenticateService authenticateService;

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


}
