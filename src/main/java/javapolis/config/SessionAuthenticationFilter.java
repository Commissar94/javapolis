package javapolis.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Transactional
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        System.out.println("🔍 Фильтр аутентификации для: " + request.getRequestURI());
        
        // Проверяем, есть ли аутентификация в текущем контексте
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        
        if (currentAuth == null || !currentAuth.isAuthenticated()) {
            // Если нет, пытаемся восстановить из сессии
            HttpSession session = request.getSession(false);
            if (session != null) {
                System.out.println("📋 Сессия найдена: " + session.getId());
                Object securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
                if (securityContext != null) {
                    System.out.println("🔐 Контекст безопасности найден в сессии");
                    if (securityContext instanceof SecurityContext) {
                        SecurityContext context = (SecurityContext) securityContext;
                        Authentication auth = context.getAuthentication();
                        if (auth != null && auth.isAuthenticated()) {
                            SecurityContextHolder.setContext(context);
                            System.out.println("✅ Восстановлена аутентификация из сессии для: " + auth.getName());
                        } else {
                            System.out.println("❌ Аутентификация в сессии недействительна");
                        }
                    } else if (securityContext instanceof org.springframework.security.core.context.SecurityContext) {
                        org.springframework.security.core.context.SecurityContext context = 
                            (org.springframework.security.core.context.SecurityContext) securityContext;
                        Authentication auth = context.getAuthentication();
                        if (auth != null && auth.isAuthenticated()) {
                            SecurityContextHolder.setContext(context);
                            System.out.println("✅ Восстановлена аутентификация из сессии для: " + auth.getName());
                        } else {
                            System.out.println("❌ Аутентификация в сессии недействительна");
                        }
                    }
                } else {
                    System.out.println("❌ Контекст безопасности не найден в сессии");
                }
            } else {
                System.out.println("❌ Сессия не найдена");
            }
        } else {
            System.out.println("✅ Аутентификация уже активна для: " + currentAuth.getName());
        }
        
        filterChain.doFilter(request, response);
    }
}
