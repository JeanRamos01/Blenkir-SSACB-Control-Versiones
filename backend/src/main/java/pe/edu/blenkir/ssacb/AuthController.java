package pe.edu.blenkir.ssacb;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager auth;
    private final UserAccountRepository users;
    private final JwtService jwt;
    public AuthController(AuthenticationManager auth, UserAccountRepository users, JwtService jwt){this.auth=auth;this.users=users;this.jwt=jwt;}

    record LoginRequest(@NotBlank String username,@NotBlank String password){}
    record AuthResponse(String token,String username,String displayName,String role){}

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req){
        auth.authenticate(new UsernamePasswordAuthenticationToken(req.username(),req.password()));
        var u=users.findByUsername(req.username()).orElseThrow();
        return new AuthResponse(jwt.generate(u),u.getUsername(),u.getDisplayName(),u.getRole().name());
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication a){
        var u=users.findByUsername(a.getName()).orElseThrow();
        return ResponseEntity.ok(Map.of("username",u.getUsername(),"displayName",u.getDisplayName(),"role",u.getRole().name()));
    }
}
