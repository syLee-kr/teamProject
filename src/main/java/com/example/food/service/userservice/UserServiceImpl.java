package com.example.food.service.userservice;

import com.example.food.domain.Users;
import com.example.food.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public Users getUser(String userId) {
        return userRepo.findByUserId(userId);
    }

    @Override
    public String findById(String userId) {
        return userRepo.findById(userId).isPresent() ? userId : null;
    }

    @Override
    public void changeUserRole(Users user, String newRole) {
        // 1. 데이터베이스에서 사용자 확인
        Users changeRole = userRepo.findByUserId(user.getUserId());
        // 2. Enum 검증 및 역할 변경
        try {
            Users.Role roleEnum = Users.Role.valueOf(newRole);
            if (roleEnum.equals(changeRole.getRole())) {
                throw new IllegalArgumentException("기존과 동일한 세팅입니다.");
            }
            changeRole.setRole(roleEnum);
            userRepo.save(changeRole); // 변경 사항 저장
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 역할 값입니다: " + newRole);
        }
    }

    @Override
    public String findMyPassword(String userId, String email) {
        Users user = userRepo.findByEmail(userId, email);
        return user.getPassword();
    }

    @Override
    public void changeUserPassword(String userId, String newPassword) {
        // 사용자 조회
        Users user = userRepo.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("유저 정보를 찾을 수 없습니다: " + userId);
        }

        // 비밀번호 암호화 로직 추가
        String encryptedPassword = passwordEncoder.encode(newPassword);
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
