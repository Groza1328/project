package ru.sibmobile.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sibmobile.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    private final UserService userService;
    
    public ApiController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean taken = userService.isUsernameTaken(username);
        return ResponseEntity.ok(Map.of("taken", taken));
    }
}

