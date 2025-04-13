package com.limed_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/secuder")
public class MainController {

    @GetMapping("/user")
    public String userAccess(Principal principal){
        System.out.println("/secured/user");
        if (principal == null){
            return null;
        }
        return principal.getName();
    }

}
