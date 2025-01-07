package com.example.camping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // PasswordEncoder 빈 등록
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // SecurityFilterChain을 사용하여 보안 설정
	@Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests -> 
                	authorizeRequests
                        .requestMatchers("/login", "/register").permitAll() // 로그인과 회원가입은 모두 접근 가능
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> 
                	formLogin
                        .loginPage("/login")  // 로그인 페이지 경로
                        .loginProcessingUrl("/login") // 로그인 처리 URL
                        .defaultSuccessUrl("/main", true) // 로그인 성공 시 redirect URL
                        .permitAll()
                )
                .logout(logout -> 
                	logout
                        .permitAll()
                );
        return http.build();
    }

}
