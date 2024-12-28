package com.example.food.service.userservice;

import com.example.food.entity.Users;
import com.example.food.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

// UserServiceImpl 수정
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<Users> getAllUsers(Pageable pageable) {
        return userRepo.findAll(pageable);
    }

    @Override
    public Users getUser(String userId) {
        return userRepo.findByUserId(userId);
    }

    @Override
    public Users getUserByNameAndPhone(String name, String phone) {
        return userRepo.findByNameAndPhone(name, phone);
    }

    @Override
    public Users getUserByIdAndPhone(String userId, String phone) {
        return userRepo.findByUserIdAndPhone(userId, phone);
    }

    @Override
    public String findById(String userId) {
        return userRepo.findById(userId).isPresent() ? userId : null;
    }

    @Override
    public void changeUserRole(Users user) {
        userRepo.save(user);
    }

    @Override
    public String findMyPassword(String userId, String phone) {
        Users user = userRepo.findByUserIdAndPhone(userId, phone);
        return user != null ? user.getPassword() : null;
    }

    @Override
    public void changeUserPassword(String userId, String newPassword) {
        // 1. 사용자 조회
        Users user = userRepo.findByUserId(userId);

        // 2. 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(newPassword);

        // 3. 암호 업데이트
        user.setPassword(encryptedPassword);

        // 4. 사용자 정보 저장
        userRepo.save(user);
    }


    @Override
    public void newUser(Users user) {
        userRepo.save(user);
    }

    @Override
    public void deleteUser(Users user) {
        userRepo.delete(user);
    }

    @Override
    public void changeUser(Users updatedUser) {
        userRepo.save(updatedUser);
    }
}
