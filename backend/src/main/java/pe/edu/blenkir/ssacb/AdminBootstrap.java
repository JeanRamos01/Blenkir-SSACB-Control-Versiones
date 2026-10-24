package pe.edu.blenkir.ssacb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrap {
    @Bean
    CommandLineRunner bootstrapAdministrator(UserAccountRepository users,PasswordEncoder encoder,
       @Value("${app.bootstrap.admin-username:admin}") String username,
       @Value("${app.bootstrap.admin-password:}") String password){
        return args -> {
            if(users.count()==0 && password!=null && password.length()>=12){
                users.save(UserAccount.builder().username(username).passwordHash(encoder.encode(password))
                    .displayName("Administración Blenkir").role(Role.ADMINISTRADOR).build());
            }
        };
    }
}
