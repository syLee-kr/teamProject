package com.example.food.service.appuserservice;

import com.example.food.domain.AppUser;

public interface AppUserService {

    //  유저 정보 가져오기
    AppUser getAppUser(String userId);

    //  특정 유저에 대한 권한 변경
    void changeAppUserRole(AppUser appUser, String newRole);

    //  비밀번호 찾기
    String findMyPassword(String userId, String email);

    //  비밀번호 변경
    void changeAppUserPassword(String userId, String newPassword);

    //  회원 가입
    void newAppUser(AppUser appUser);

    //  회원 탈퇴
    void deleteAppUser(AppUser appUser);

    //  회원 정보 수정
    void changeAppUser(AppUser appUser);
}
