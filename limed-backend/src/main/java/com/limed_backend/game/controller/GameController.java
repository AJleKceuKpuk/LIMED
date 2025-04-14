package com.limed_backend.game.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

    @GetMapping("/game")
    public String gameAccess() {
        return "Добро пожаловать в игру! Доступ разрешён только для авторизованных пользователей.";
    }
}