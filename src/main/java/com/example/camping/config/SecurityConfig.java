package com.example.camping.config;

import com.example.camping.repository.UserRepository;
import com.example.camping.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UserRepository userRepo;

    // UserRepository를 생성자로 주입
    public SecurityConfig(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // CustomUserDetailsService 등록
    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService(userRepo);
    }

    // 비밀번호 암호화를 위한 PasswordEncoder 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // SecurityFilterChain 등록
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 설정 (필요 시 비활성화)
                .csrf(csrf -> csrf.disable())

                // 권한 및 인증 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/admin/**").hasRole("ADMIN")  // ADMIN 권한이 필요한 경로
                        .requestMatchers("/user/**").hasRole("USER")    // USER 권한이 필요한 경로
                        .anyRequest().permitAll()                      // 나머지 요청은 모두 허용
                )

                // 로그인 설정
                .formLogin(form -> form
                        .loginPage("/login")                           // 커스텀 로그인 페이지 경로
                        .defaultSuccessUrl("/")                        // 로그인 성공 후 이동할 경로
                        .permitAll()
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")                          // 로그아웃 URL
                        .logoutSuccessUrl("/")                         // 로그아웃 성공 후 이동 경로
                        .permitAll()
                );

        return http.build();
    }
}
