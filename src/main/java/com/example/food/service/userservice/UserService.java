package com.example.food.service.userservice;

import com.example.food.domain.Users;

public interface UserService {

    //  유저 정보 가져오기
    Users getUser(String userId);

    String findById(String userId);

    //  특정 유저에 대한 권한 변경
    void changeUserRole(Users user, String newRole);

    //  비밀번호 찾기
    String findMyPassword(String userId, String email);

    //  비밀번호 변경
    void changeUserPassword(String userId, String newPassword);

    //  회원 가입
    void newUser(Users user);

    //  회원 탈퇴
    void deleteUser(Users user);

    //  회원 정보 수정
    void changeUser(Users user);
}
