package com.limed_backend.game.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {
    //@PreAuthorize("@banChecker.isNotBanned(authentication.name)")
    @GetMapping("/game")
    public ResponseEntity<String> playGame() {
        return ResponseEntity.ok("Добро пожаловать в игру!");
    }

}