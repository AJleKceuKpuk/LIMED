package limed_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @GetMapping("/admin")
    public String adminAccess() {
        return "Добро пожаловать, ADMIN! Здесь вы управляете системой.";
    }
}