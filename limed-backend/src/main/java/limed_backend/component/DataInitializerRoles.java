package limed_backend.component;

import limed_backend.models.Role;
import limed_backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializerRoles implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializerRoles(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            Role userRole = Role.builder().id(1L).name("USER").build();
            Role adminRole = Role.builder().id(2L).name("ADMIN").build();
            Role moderatorRole = Role.builder().id(3L).name("MODERATOR").build();

            roleRepository.saveAll(List.of(userRole, adminRole, moderatorRole));
        }
    }
}

