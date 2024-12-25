package com.example.food.config;

import java.io.File;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {  // WebMvcConfigurer 인터페이스 구현

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry rhregistry) {
        // 현재 시스템의 실행 디렉토리 기준으로 업로드된 이미지 제공
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploadImg";  // 현재 디렉토리 기준

        // 정적 리소스를 "/uploadImg/**" 경로로 매핑
        rhregistry.addResourceHandler("/uploadImg/**")
                .addResourceLocations("file:" + uploadDir + "/"); // file: 프로토콜로 시스템 파일 경로 지정
    }
}
