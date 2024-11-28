package com.example.food.config;

import com.example.food.domain.User;
import com.example.food.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncryptionExample {

    private final BCryptPasswordEncoder pe;
    private final UserRepository aur;

    @Autowired
    public PasswordEncryptionExample(UserRepository aur, BCryptPasswordEncoder pe) {
        this.aur = aur;
        this.pe = pe;
    }

    public void encryptAndSavePassword(String rawPassword, String username) {
        // 1. 비밀번호 암호화
        String encodedPassword = pe.encode(rawPassword);

        // 2. 사용자 저장
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        userRepository.save(user); // DB에 저장

        System.out.println("User saved: " + username + " with encrypted password.");
    }

    public boolean verifyPassword(String username, String rawPassword) {
        // 1. DB에서 사용자 조회
        User user = userRepository.findByUsername(username);

        if (user == null) {
            System.out.println("User not found: " + username);
            return false;
        }

        // 2. 비밀번호 검증
        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
        System.out.println("Password match for " + username + ": " + matches);
        return matches;
    }
}
