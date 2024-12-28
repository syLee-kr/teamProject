package com.example.food.service;

import com.example.food.entity.Users;
import com.example.food.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Users getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("인증되지 않은 사용자 요청입니다.");
            return null;
        }

        Object principal = authentication.getPrincipal();
        String userId = extractUserId(principal);
        logger.debug("Extracted userId: {}", userId);

        if (userId != null) {
            // 주의: 여기서 findById(userId)를 쓴다는 것은, DB의 PK가 "이메일"로 되어 있는 경우입니다.
            // 만약 Users 테이블의 PK가 email이 아니라면, 별도의 findByEmail(userId) 메서드를 사용하세요.
            Users user = userRepository.findByUserId(userId);
            if (user != null) {
                logger.info("인증된 사용자 정보: {}", user.getUserId());
                return user;
            } else {
                logger.warn("사용자 ID {}가 데이터베이스에 존재하지 않습니다.", userId);
            }
        } else {
            logger.warn("principal에서 사용자 ID를 추출할 수 없습니다.");
        }

        return null;
    }

    /**
     * principal 객체에서 사용자 ID(여기서는 '이메일')를 추출합니다.
     *
     * @param principal 인증 principal 객체
     * @return 사용자 ID(이메일) 또는 null
     */
    public String extractUserId(Object principal) {
        // 1) OAuth2User 타입 (네이버 or 구글 OAuth2)
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;

            // (1) 구글 등의 경우 top-level "email"만 바로 꺼내면 됨
            String topLevelEmail = oauth2User.getAttribute("email");
            if (topLevelEmail != null) {
                return topLevelEmail;
            }

            // (2) 네이버의 경우, attributes = { "resultcode":.., "message":.., "response": {...} }
            Map<String, Object> attrs = oauth2User.getAttributes();
            if (attrs.containsKey("response")) {
                Map<String, Object> response = (Map<String, Object>) attrs.get("response");
                if (response != null) {
                    String naverEmail = (String) response.get("email");
                    if (naverEmail != null) {
                        return naverEmail;
                    }
                }
            }
        }
        // 2) 일반 로그인 (UserDetails)
        else if (principal instanceof org.springframework.security.core.userdetails.User) {
            return ((org.springframework.security.core.userdetails.User) principal).getUsername();
        }

        // 3) 그 외
        return null;
    }

    /**
     * 사용자가 인증되었는지 확인합니다.
     *
     * @return 인증되었으면 true, 아니면 false
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String
                && "anonymousUser".equals(authentication.getPrincipal()));
    }
}
