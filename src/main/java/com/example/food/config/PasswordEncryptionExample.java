package com.example.food.config;

import com.example.food.domain.Users;
import com.example.food.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncryptionExample {

    private final BCryptPasswordEncoder pe;
    private final UserRepository ur;

    @Autowired
    public PasswordEncryptionExample(UserRepository ur, BCryptPasswordEncoder pe) {
        this.ur = ur;
        this.pe = pe;
    }

    public void encryptAndSavePassword(String password, String username) {
        // 1. 비밀번호 암호화
        String encodedPassword = pe.encode(password);

        // 2. 사용자 저장
        Users user = new Users();
        user.setUserId(username);
        // 3. 암호화 된 비밀번호 저장
        user.setPassword(encodedPassword);
        ur.save(user);

        System.out.println("User saved: " + username + " with encrypted password.");
    }

    //  로그인 시 비밀번호 확인 하는 로직
    public boolean verifyPassword(String userId, String rawPassword) {
        // 1. DB에서 사용자 조회
        Users user = ur.findByUserId(userId);

        // 2. 유저 정보가 없다면, false
        if (user == null) {
            System.err.println("User not found: " + userId);
            return false;
        }

        // 2. 비밀번호 검증
        // rawPassword는 로그인 시 입력된 비밀번호, user.getPassword()는 암호화 되어 DB에 저장된 비밀번호
        // pe.matches 는 암호화 된 비밀번호를 가져와서 암호를 역순으로 진행, 일반 비밀번호로 변경 후 입력한 비밀번호와 일치하는지 확인
        boolean matches = pe.matches(rawPassword, user.getPassword());

        if (matches) {
            System.out.println("Password verification succeeded for user: " + userId);
        } else {
            System.err.println("Password verification failed for user: " + userId);
        }

        return matches;
    }
}
