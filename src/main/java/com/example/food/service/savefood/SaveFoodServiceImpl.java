package com.example.food.service.savefood;

import com.example.food.domain.SaveFood;
import com.example.food.repository.SaveFoodRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaveFoodServiceImpl implements SaveFoodService{

    private final SaveFoodRepository saveFoodRepository;

    public SaveFoodServiceImpl(SaveFoodRepository saveFoodRepository) {
        this.saveFoodRepository = saveFoodRepository;
    }

    public List<SaveFood> getUserFood(String userId) {
        return saveFoodRepository.findAllByUser_UserId(userId);
    }
}
