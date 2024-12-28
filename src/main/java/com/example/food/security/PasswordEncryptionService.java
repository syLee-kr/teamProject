package com.example.food.security;

import com.example.food.entity.Users;
import com.example.food.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class PasswordEncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordEncryptionService.class);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public PasswordEncryptionService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void encryptAndSavePassword(String password, String username) {
        // 1. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // 2. 사용자 저장 또는 업데이트
        Users user = userRepository.findByUserId(username);
        user.setUserId(username);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        logger.info("사용자 저장 완료: {} (암호화된 비밀번호 저장)", username);
    }
}
