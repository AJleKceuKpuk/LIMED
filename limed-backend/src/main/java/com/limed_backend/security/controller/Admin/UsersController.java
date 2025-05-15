package com.limed_backend.security.controller.Admin;

import com.limed_backend.security.dto.User.UpdateEmailRequest;
import com.limed_backend.security.dto.User.UpdateRoleRequest;
import com.limed_backend.security.dto.User.UpdateUsernameRequest;
import com.limed_backend.security.dto.User.UserResponse;
import com.limed_backend.security.service.*;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    /** Получение всех пользователей постранично*/
    @GetMapping("/get-allusers")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Page<UserResponse> users = userService.getAllUsers(size, page);
        return ResponseEntity.ok(users);
    }
    /** Получение пользователя по его ID*/
    @GetMapping("/get-user/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        UserResponse user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }
    /** Изменение имени пользователя принудительно*/
    @PutMapping("/edit-username/{id}")
    public ResponseEntity<String> editUsername(@RequestBody UpdateUsernameRequest request, @PathVariable Long id) {
        String result = userService.editUsername(request, id);
        return ResponseEntity.ok(result);
    }
    /** Изменение Email пользователя принудительно*/
    @PutMapping("/edit-email/{id}")
    public ResponseEntity<String> editEmail(@RequestBody UpdateEmailRequest request, @PathVariable Long id) {
        String result = userService.editEmail(request, id);
        return ResponseEntity.ok(result);
    }
    /** Изменение Ролей пользователя*/
    @PutMapping("/edit-role")
    public ResponseEntity<String> editRole(@RequestBody UpdateRoleRequest request) {
        String result = userService.editRole(request);

        return ResponseEntity.ok(result);
    }
}
