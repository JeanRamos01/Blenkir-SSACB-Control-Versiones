package pe.edu.blenkir.ssacb;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DataSeeder {
    @Bean
    CommandLineRunner seedDemoData(DemoDataInitializer initializer) {
        return args -> initializer.initialize();
    }
}
