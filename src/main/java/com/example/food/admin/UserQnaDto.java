package com.example.food.admin;


import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class UserQnaDto {
    private String userId;
    private String userName;
    private String lastQuestion;
    private OffsetDateTime lastQDate;

    public UserQnaDto(String userId, String userName, String lastQuestion, OffsetDateTime lastQDate) {
        this.userId = userId;
        this.userName = userName;
        this.lastQuestion = lastQuestion;
        this.lastQDate = lastQDate;
    }

}
