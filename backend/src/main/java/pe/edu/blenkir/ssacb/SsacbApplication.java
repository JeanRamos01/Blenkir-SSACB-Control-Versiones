package pe.edu.blenkir.ssacb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SsacbApplication {
    public static void main(String[] args) {
        SpringApplication.run(SsacbApplication.class, args);
    }
}
