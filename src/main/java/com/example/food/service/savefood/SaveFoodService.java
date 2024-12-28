package com.example.food.service.savefood;

import com.example.food.entity.SaveFood;

import java.util.List;

public interface SaveFoodService {
    List<SaveFood> getUserFood(String userId);
    void deleteSaveFood(Long sfSeq);
    SaveFood saveSaveFood(SaveFood saveFood);
}
