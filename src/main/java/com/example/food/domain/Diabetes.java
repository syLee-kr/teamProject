package com.example.food.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Diabetes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 당뇨병 데이터 ID

    private Double fastingBloodSugar;   // 공복혈당 (mg/dL)
    private Double totalCholesterol;    // 총콜레스테롤
    private Double triglycerides;       // 중성지방
    private Double ldlCholesterol;      // 나쁜 (LDL) 콜레스테롤
    private Double hdlCholesterol;      // 좋은 (HDL) 콜레스테롤
}
