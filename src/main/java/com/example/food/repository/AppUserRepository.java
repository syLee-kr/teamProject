package com.example.food.repository;

import com.example.food.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    // 유저 ID를 바탕으로 유저 정보를 가져온다.
    AppUser findByUsername(String userId);

    // 유저 이메일과 아이디를 바탕으로 유저 정보를 알려준다.
    AppUser findByEmail(String userId, String email);

    //  유저 아이디와 신규 비밀번호를 전달받고 비밀번호를 변경
    @Transactional
    @Modifying
    @Query("UPDATE AppUser m SET m.password = :password WHERE m.username = :username")
    void changePassword(@Param("username") String username, @Param("password") String password);
}
