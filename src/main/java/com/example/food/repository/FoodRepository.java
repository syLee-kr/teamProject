package com.example.food.repository;

import com.example.food.domain.SaveFood;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<SaveFood, Long> {
}
