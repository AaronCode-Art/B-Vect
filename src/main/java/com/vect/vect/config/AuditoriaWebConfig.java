package com.vect.vect.config;

import com.vect.vect.entity.Usuario;
import com.vect.vect.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuditoriaWebConfig implements WebMvcConfigurer {

    private final AuditoriaService auditoriaService;

    public AuditoriaWebConfig(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CambiosApiInterceptor(auditoriaService)).addPathPatterns("/api/**");
    }

    private static final class CambiosApiInterceptor implements HandlerInterceptor {

        private final AuditoriaService auditoriaService;

        private CambiosApiInterceptor(AuditoriaService auditoriaService) {
            this.auditoriaService = auditoriaService;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                    Object handler, Exception exception) {
            if (exception != null || response.getStatus() < 200 || response.getStatus() >= 400
                    || !esEscritura(request.getMethod())) {
                return;
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Usuario actor) {
                auditoriaService.registrarCambio(request, actor);
            }
        }

        private boolean esEscritura(String method) {
            return "POST".equals(method) || "PUT".equals(method)
                || "PATCH".equals(method) || "DELETE".equals(method);
        }
    }
}
