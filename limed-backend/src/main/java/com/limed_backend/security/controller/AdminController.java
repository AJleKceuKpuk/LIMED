package com.limed_backend.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {



    @GetMapping("/start")
    public String adminAccess() {
        return "Добро пожаловать, ADMIN! Здесь вы управляете системой.";
    }


}