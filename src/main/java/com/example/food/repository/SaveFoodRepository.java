package com.example.food.repository;

import com.example.food.domain.SaveFood;
import com.example.food.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaveFoodRepository extends JpaRepository<SaveFood, Long> {
    List<SaveFood> findAllByUser_UserId(String userId);

    void deleteById(Long sfSeq);
}
