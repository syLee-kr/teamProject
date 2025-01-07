package com.example.camping.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;
import com.example.camping.userService.UserServiceImpl;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	
	private UserRepository userRepo;
	
	private UserServiceImpl userServiceImpl;
	
	private PasswordEncoder passwordEncoder;
	
    // PasswordEncoder 빈 등록
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // 로그인 인증 처리
    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    	AuthenticationManagerBuilder authenticationManagerBuilder =
    	
    	http
    		.getSharedObject(AuthenticationManagerBuilder.class);
    				
    	authenticationManagerBuilder
    		.userDetailsService(userServiceImpl) // UserServiceImpl을 UserDetailsService로 사용
			.passwordEncoder(passwordEncoder); // 비밀번호 인코딩
		
    	return authenticationManagerBuilder
    			.build();
    }
    
    
	// 관리자 계정 생성
	@PostConstruct
	public void initAdminUser() {
		// 관리자 계정 확인
		Users adminUser = userRepo.findByUserId("admin_user");
		
		if (adminUser == null) {
			// 관리자 계정 없을 시 새로 생성
			adminUser = new Users();
			adminUser.setUserId("admin_user");
			adminUser.setName("관리자");
			adminUser.setPassword(passwordEncoder.encode("admin_password")); // 관리자 비밀번호 암호화
			adminUser.setRole(Users.Role.ROLE_ADMIN); // 관리자 권한 설정
			userRepo.save(adminUser); // DB에 관리자 계정 저장
		}
	}


    // SecurityFilterChain을 사용하여 보안 설정
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeRequests -> 
                	authorizeRequests
                        	.requestMatchers("/login", "/register").permitAll() // 로그인과 회원가입은 모두 접근 가능
                        	.requestMatchers("/admin/**").hasRole("ADMIN") // 관리자 전용 경로
                        	.anyRequest().authenticated() // 그외 모든 요청 인증
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
                )
        		.build();
    }

}
