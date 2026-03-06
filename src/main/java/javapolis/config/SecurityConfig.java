package javapolis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import javapolis.model.User;
import javapolis.repository.UserRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@Transactional
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(new SessionAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/index.html", "/login", "/register", "/api/auth/**", "/api/registration/**", "/h2-console/**").permitAll()
                .requestMatchers("/university/**", "/api/university/**").permitAll()
                .requestMatchers("/api/auth/status").permitAll()
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/assets/**").permitAll()
                .requestMatchers("/dashboard", "/city", "/api/dashboard", "/api/city", "/api/forum/**", "/api/business/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterAfter(new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                    filterChain.doFilter(request, response);
                }
            }, UsernamePasswordAuthenticationFilter.class)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .and()
                .sessionFixation().migrateSession()
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":true,\"message\":\"Logout successful\"}");
                    response.getWriter().flush();
                })
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        
        // Для H2 консоли
        http.headers(headers -> headers.frameOptions().disable());
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                // Сначала проверяем базу данных
                try {
                    User user = userRepository.findByUsername(username)
                        .orElse(null);
                    
                    if (user != null) {
                        if (!user.isEmailVerified()) {
                            throw new UsernameNotFoundException("Email не подтвержден для пользователя: " + username);
                        }
                        
                        if (!user.isEnabled()) {
                            throw new UsernameNotFoundException("Пользователь заблокирован: " + username);
                        }
                        
                        return org.springframework.security.core.userdetails.User.builder()
                            .username(user.getUsername())
                            .password(user.getPassword())
                            .roles(user.getRole().name())
                            .disabled(!user.isEnabled())
                            .accountExpired(false)
                            .credentialsExpired(false)
                            .accountLocked(false)
                            .build();
                    }
                } catch (Exception e) {
                    // Ошибка загрузки пользователя из БД
                }
                
                            // Если пользователь не найден в БД, создаем временного админа
            if ("admin".equals(username)) {
                return org.springframework.security.core.userdetails.User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .roles("ADMIN")
                    .disabled(false)
                    .accountExpired(false)
                    .credentialsExpired(false)
                    .accountLocked(false)
                    .build();
            }
                
                throw new UsernameNotFoundException("Пользователь не найден: " + username);
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
