package com.example.food.service.savefood;

import com.example.food.domain.SaveFood;
import com.example.food.repository.SaveFoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaveFoodServiceImpl implements SaveFoodService{

    private final SaveFoodRepository saveFoodRepository;

    @Autowired
    public SaveFoodServiceImpl(SaveFoodRepository saveFoodRepository) {
        this.saveFoodRepository = saveFoodRepository;
    }

    @Override
    public List<SaveFood> getUserFood(String userId) {
        return saveFoodRepository.findAllByUser_UserId(userId);
    }
    @Override
    public void deleteSaveFood(Long sfSeq) {
        saveFoodRepository.deleteById(sfSeq);
    }
    @Override
    public SaveFood saveSaveFood(SaveFood saveFood) {
        return saveFoodRepository.save(saveFood);
    }
}
