package com.example.food.repository;

import com.example.food.entity.SaveFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SaveFoodRepository extends JpaRepository<SaveFood, Long> {

    @Query("SELECT DISTINCT sf FROM SaveFood sf JOIN FETCH sf.menus WHERE sf.user.userId = :userId ORDER BY sf.saveDate ASC")
    List<SaveFood> findAllWithMenusByUserId(@Param("userId") String userId);
    void deleteById(Long sfSeq);
}
