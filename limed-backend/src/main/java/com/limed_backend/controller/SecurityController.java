package com.limed_backend.controller;

import com.limed_backend.component.JwtCore;
import com.limed_backend.component.SinginRequest;
import com.limed_backend.component.SingupRequest;
import com.limed_backend.entity.Users;
import com.limed_backend.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class SecurityController {

    private UsersRepository usersRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtCore jwtCore;

    @Autowired
    public void setUsersRepository(UsersRepository usersRepository){
        this.usersRepository = usersRepository;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager){
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setJwtCore(JwtCore jwtCore){
        this.jwtCore = jwtCore;
    }

    @PostMapping("/singup")
    ResponseEntity<?> singup(@RequestBody SingupRequest singupRequest){
        if (usersRepository.existsUserByUsername(singupRequest.getUsername())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose differen name");
        }
        if (usersRepository.existsUserByEmail(singupRequest.getEmail())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose differen email");
        }
        Users user = new Users();
        user.setUsername(singupRequest.getUsername());
        user.setEmail(singupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(singupRequest.getPassword()));
        user.setRoles(singupRequest.getRoles());
        usersRepository.save(user);
        return ResponseEntity.ok("Success");

    }

    public @PostMapping("/singin")
    ResponseEntity<?> singin(@RequestBody SinginRequest singinRequest){
        Authentication authentication = null;
        System.out.println("Login");
        try{
            System.out.println("try");
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    singinRequest.getUsername(),
                    singinRequest.getPassword()));

        }catch (BadCredentialsException e){
            System.out.println("Excepcion");
            ResponseEntity.ok("Not Succes");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtCore.generateToken(authentication);
        return ResponseEntity.ok(jwt);
    }

}
