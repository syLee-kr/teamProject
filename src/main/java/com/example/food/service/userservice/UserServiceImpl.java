package com.example.food.service.userservice;

import com.example.food.domain.User;
import com.example.food.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository UserRepo;

    @Autowired
    public UserServiceImpl(UserRepository UserRepo) {
        this.UserRepo = UserRepo;
    }

    @Override
    public User getUser(String userId) {
        return UserRepo.findByUsername(userId);
    }

    @Override
    public void changeUserRole(User user, String newRole) {
        // 1. 데이터베이스에서 사용자 확인
        User changeRole = UserRepo.findByUsername(user.getUserId());
        if (changeRole == null) {
            throw new IllegalArgumentException("아이디를 찾을 수 없습니다: " + user.getUserId());
        }

        // 2. Enum 검증 및 역할 변경
        try {
            User.Role roleEnum = User.Role.valueOf(newRole);
            if (roleEnum.equals(changeRole.getRole())) {
                throw new IllegalArgumentException("기존과 동일한 세팅입니다.");
            }
            changeRole.setRole(roleEnum);
            UserRepo.save(changeRole); // 변경 사항 저장
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 역할 값입니다: " + newRole);
        }
    }

    @Override
    public String findMyPassword(String userId, String email) {
        User appUser = UserRepo.findByEmail(userId, email);
        if (appUser != null) {
            // 비밀번호 직접 반환 대신, 별도 복구 프로세스를 구현하도록 리팩토링 필요
            return "비밀번호 복구 프로세스를 완료하세요.";
        } else {
            throw new IllegalArgumentException("해당 정보를 가진 사용자를 찾을 수 없습니다.");
        }
    }

    @Override
    public void changeUserPassword(String userId, String newPassword) {
        // 사용자 조회
        User user = UserRepo.findByUsername(userId);
        if (user == null) {
            throw new IllegalArgumentException("유저 정보를 찾을 수 없습니다: " + userId);
        }

        // 비밀번호 암호화 로직 추가
        // String encryptedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(newPassword); // 암호화 적용 후 변경
        UserRepo.save(user);
    }

    @Override
    public void newUser(User user) {
        // 1. 역할 기본값 설정
        if (user.getRole() == null) {
            user.setRole(User.Role.ROLE_USER);
        }

        // 2. 데이터 저장
        UserRepo.save(user);
    }

    @Override
    public void deleteUser(User user) {
        UserRepo.delete(user);
    }

    @Override
    public void changeUser(User updatedUser) {
        //  대부분의 검증 로직은 컨트롤러와 JS에서 작업할 예정
        UserRepo.save(updatedUser);
    }
}
