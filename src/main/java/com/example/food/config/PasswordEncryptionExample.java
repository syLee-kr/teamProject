//package com.example.food.config;
//
//import com.example.food.domain.Users;
//import com.example.food.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Component
//public class PasswordEncryptionExample {
//
////    private final BCryptPasswordEncoder pe;
//    private final UserRepository ur;
//
//    @Autowired
//    public PasswordEncryptionExample(UserRepository ur /*, *BCryptPasswordEncoder pe*/) {
//        this.ur = ur;
////        this.pe = pe;
//    }
//
//    public void encryptAndSavePassword(String password, String username) {
//        // 1. 비밀번호 암호화
////        String encodedPassword = pe.encode(rawPassword);
//
//        // 2. 사용자 저장
//        Users user = new Users();
//        user.setUserId(username);
//        user.setPassword(password);
//        ur.save(user);
//
//        System.out.println("User saved: " + username + " with encrypted password.");
//    }
//
//    public boolean verifyPassword(String username, String rawPassword) {
//        // 1. DB에서 사용자 조회
//        Users user = ur.findByUsername(username);
//
//        if (user == null) {
//            System.out.println("User not found: " + username);
//            return false;
//        }
//
//        // 2. 비밀번호 검증
////        boolean matches = pe.matches(rawPassword, user.getPassword());
//        System.out.println("Password match for " + username + ": " + matches);
//        return matches;
//    }
//}
