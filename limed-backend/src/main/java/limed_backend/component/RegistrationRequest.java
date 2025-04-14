package limed_backend.component;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String username;
    private String password;
}