package pe.edu.blenkir.ssacb;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger logger=LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtService jwt;
    private final UserDetailsService uds;
    public JwtAuthFilter(JwtService jwt, UserDetailsService uds){ this.jwt=jwt; this.uds=uds; }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String auth=req.getHeader("Authorization");
        if(auth==null || !auth.startsWith("Bearer ")) { chain.doFilter(req,res); return; }
        String token=auth.substring(7);
        String username;
        try {
            username=jwt.username(token);
        } catch(JwtException | IllegalArgumentException exception) {
            logger.debug("Ignoring invalid bearer token ({})",exception.getClass().getSimpleName());
            chain.doFilter(req,res);
            return;
        }
        if(username==null || username.isBlank()){
            logger.debug("Ignoring bearer token without a username");
            chain.doFilter(req,res);
            return;
        }
        if(SecurityContextHolder.getContext().getAuthentication()==null){
            UserDetails userDetails;
            try{
                userDetails=uds.loadUserByUsername(username);
            }catch(UsernameNotFoundException exception){
                logger.debug("Ignoring bearer token for unknown user");
                chain.doFilter(req,res);
                return;
            }
            if(userDetails.isEnabled() && userDetails.isAccountNonLocked() && userDetails.isAccountNonExpired()
                && userDetails.isCredentialsNonExpired() && jwt.valid(token,userDetails.getUsername())){
                var authentication=new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(req,res);
    }
}
