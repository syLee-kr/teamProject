package com.example.food.repository;

import com.example.food.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<Users, String> {

    Page<Users> findAll(Pageable pageable);

    // 유저 ID를 바탕으로 유저 정보를 가져온다.
    Users findByUserId(String userId);

    @Query("SELECT u FROM Users u WHERE u.name = :name AND u.phone = :phone")
    Users findByNameAndPhone(@Param("name") String name, @Param("phone") String phone);

    Users findByUserIdAndPhone(String userId, String phone);

    // 유저 이메일과 아이디를 바탕으로 유저 정보를 알려준다.
    @Query("SELECT u FROM Users u WHERE u.phone = ?1 AND u.userId = ?2")
    Users findByPhone(String phone, String userId); // 매개변수 순서 수정

    //  유저 아이디와 신규 비밀번호를 전달받고 비밀번호를 변경
    @Transactional
    @Modifying
    @Query("UPDATE Users m SET m.password = :password WHERE m.userId = :username")
    void changePassword(@Param("username") String username, @Param("password") String password);
}
