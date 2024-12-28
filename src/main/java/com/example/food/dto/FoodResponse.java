package com.example.food.dto;

import com.example.food.entity.Menu;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class FoodResponse {
    private Long sfSeq;
    private OffsetDateTime saveDate;
    private String mealType;
    private List<Menu> menus;
}
