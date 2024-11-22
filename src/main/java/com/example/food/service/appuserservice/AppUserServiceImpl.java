package com.example.food.service.appuserservice;

import com.example.food.domain.AppUser;
import com.example.food.repository.AppUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepo;

    @Autowired
    public AppUserServiceImpl(AppUserRepository appUserRepo) {
        this.appUserRepo = appUserRepo;
    }

    @Override
    public AppUser getAppUser(String userId) {
        return appUserRepo.findByUsername(userId);
    }

    @Override
    public void changeAppUserRole(AppUser appUser, String newRole) {

        // 1. 데이터 베이스에서 사용자 확인
        AppUser changeRole = appUserRepo.findByUsername(appUser.getUserId());
        if (changeRole == null) {
            throw new IllegalArgumentException("아이디를 찾을 수 없습니다: " + appUser.getUserId());
        }

        // 2. 새로운 역할이 기존 역할과 동일한 지 확인
        if (newRole.equals(changeRole.getRole())) {
            throw new IllegalArgumentException("기존과 동일한 세팅입니다.");
        }

        // 3. 역할 변경 및 저장
        changeRole.setRole(newRole);
        appUserRepo.save(changeRole); // DB에 저장
    }

    //  로직 수정 필요. 작업 이후 재설정 기능으로 수정해야함.
    @Override
    public String findMyPassword(String userId, String email) {
        AppUser appUser = appUserRepo.findByEmail(userId,email);
        if (appUser != null) {
            return appUser.getPassword();
        } else {
            return null;
        }
    }

    @Override
    public void changeAppUserPassword(String userId, String newPassword) {
        // 사용자 조회
        AppUser user = appUserRepo.findByUsername(userId);
        if (user == null) {
            throw new IllegalArgumentException("유저 정보를 찾을 수 없습니다: " + userId);
        }

        //  비밀번호 암호화. 프로그램 완성 시에 적용할 것.
        //  String encryptedPassword = passwordEncoder.encode(newPassword);

        // 암호화된 비밀번호 저장. 일단은 바로 저장.
        user.setPassword(newPassword);
        appUserRepo.save(user); // 변경 사항 반영
    }

    @Override
    public void newAppUser(AppUser appUser) {
//        // 1. 비밀번호 암호화
//        String encryptedPassword = passwordEncoder.encode(appUser.getPassword());
//        appUser.setPassword(encryptedPassword);

        // 2. 데이터 저장
        appUserRepo.save(appUser);
        //  아이디 중복, 입력 검증 같은 로직들은 JS를 활용할 예정.
    }

    @Override
    public void deleteAppUser(AppUser appUser) {
        appUserRepo.delete(appUser);
    }

    @Override
    public void changeAppUser(AppUser updatedUser) {
        //  대부분의 검증 로직은 컨트롤러와 js에서 작업할 예정
        appUserRepo.save(updatedUser);
    }
}
