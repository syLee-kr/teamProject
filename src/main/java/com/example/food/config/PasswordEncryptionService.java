package com.example.food.config;

import com.example.food.domain.Users;
import com.example.food.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncryptionService  {


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public PasswordEncryptionService (UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void encryptAndSavePassword(String password, String username) {
        // 1. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 2. 사용자 저장
        Users user = new Users();
        user.setUserId(username);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        System.out.println("User saved: " + username + " with encrypted password.");
    }

    // 로그인 시 비밀번호 확인 하는 로직
    public boolean verifyPassword(String userId, String rawPassword) {
        // 1. DB에서 사용자 조회
        Users user = userRepository.findByUserId(userId);

        // 2. 유저 정보가 없다면, false
        if (user == null) {
            System.err.println("User not found: " + userId);
            return false;
        }

        // 3. 비밀번호 검증
        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());

        if (matches) {
            System.out.println("Password verification succeeded for user: " + userId);
        } else {
            System.err.println("Password verification failed for user: " + userId);
        }

        return matches;
    }
}