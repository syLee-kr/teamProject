package com.example.food.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GcsUploadService {

    private final Storage storage;

    // application.properties에 설정된 bucket 이름 주입
    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    /**
     * 단일 파일 업로드 후 다운로드(접근) 가능한 URL 반환
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 파일 이름: UUID + _원본파일명 형태로 생성
        String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 메타정보 구성
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo
                .newBuilder(blobId)
                // 필요한 ACL(공개 등)을 설정할 수도 있음
                //.setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER))))
                .build();

        // 실제 업로드
        storage.create(blobInfo, file.getBytes());

        // GCS 내 public URL 형태 (버킷이 공개 설정이 되어있거나, 서빙 가능한 방식일 때)
        // 버킷 공개 설정에 따라 URL이 다를 수 있으니 유의하세요.
        // 일반적으로 "storage.googleapis.com" 형식을 많이 사용합니다.
        String fileUrl = "https://storage.googleapis.com/" + bucketName + "/" + objectName;
        return fileUrl;
    }
}
