package pe.edu.blenkir.ssacb;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserDetailsService uds;
    public JwtAuthFilter(JwtService jwt, UserDetailsService uds){ this.jwt=jwt; this.uds=uds; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String auth=req.getHeader("Authorization");
        if(auth==null || !auth.startsWith("Bearer ")) { chain.doFilter(req,res); return; }
        String token=auth.substring(7);
        try {
            String username=jwt.username(token);
            if(SecurityContextHolder.getContext().getAuthentication()==null){
                var ud=uds.loadUserByUsername(username);
                if(ud.isEnabled() && ud.isAccountNonLocked() && ud.isAccountNonExpired()
                    && ud.isCredentialsNonExpired() && jwt.valid(token,ud.getUsername())){
                    var authentication=new UsernamePasswordAuthenticationToken(ud,null,ud.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch(Exception ignored) { }
        chain.doFilter(req,res);
    }
}
