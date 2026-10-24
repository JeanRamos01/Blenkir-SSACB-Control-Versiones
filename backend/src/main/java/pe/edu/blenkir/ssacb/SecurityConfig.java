package pe.edu.blenkir.ssacb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtFilter;
    private final CustomUserDetailsService uds;
    @Value("${app.cors.allowed-origin}") private String allowedOrigin;

    public SecurityConfig(JwtAuthFilter jwtFilter, CustomUserDetailsService uds){ this.jwtFilter=jwtFilter; this.uds=uds; }

    @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }

    @Bean DaoAuthenticationProvider authProvider(PasswordEncoder encoder){
        var p=new DaoAuthenticationProvider();
        p.setUserDetailsService(uds); p.setPasswordEncoder(encoder); return p;
    }

    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception { return cfg.getAuthenticationManager(); }

    @Bean SecurityFilterChain security(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
        http.csrf(csrf->csrf.disable())
            .cors(cors->cors.configurationSource(corsSource()))
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(provider)
            .authorizeHttpRequests(a->a
                .requestMatchers("/api/auth/**","/actuator/health").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean CorsConfigurationSource corsSource(){
        var c=new CorsConfiguration();
        c.setAllowedOrigins(List.of(allowedOrigin));
        c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization","Content-Type"));
        c.setAllowCredentials(true);
        var s=new UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/**",c); return s;
    }
}
