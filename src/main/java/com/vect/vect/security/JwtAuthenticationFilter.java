package com.vect.vect.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vect.vect.entity.Usuario;
import com.vect.vect.repository.UsuarioRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER.length()).trim();
        try {
            Claims claims = jwtService.parseAccessToken(token);
            UUID userId = jwtService.getUserId(claims);

            usuarioRepository.findById(userId).ifPresent(usuario -> {
                if (usuario.getActivo()) {
                    List<SimpleGrantedAuthority> roles = List.of(
                        new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())
                    );
                    var auth = new UsernamePasswordAuthenticationToken(usuario, null, roles);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            });
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
