package limed_backend.component;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
