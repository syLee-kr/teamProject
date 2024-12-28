package com.example.food.security;

import com.example.food.entity.Users;
import com.example.food.service.userservice.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("OAuth2 사용자 정보를 로드하기 시작합니다. 클라이언트 등록 ID: {}",
                userRequest.getClientRegistration().getRegistrationId());

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User;

        try {
            oAuth2User = delegate.loadUser(userRequest);
            logger.info("OAuth2 사용자 정보를 성공적으로 로드했습니다. 사용자 속성: {}",
                    oAuth2User.getAttributes());
        } catch (Exception e) {
            logger.error("OAuth2 사용자 정보를 로드하는 중 오류가 발생했습니다: {}", e.getMessage());
            throw new OAuth2AuthenticationException("oauth_error");
        }

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 네이버 또는 Google에서 이메일을 추출
        String email = parseEmail(registrationId, attributes);
        if (email == null || email.trim().isEmpty()) {
            logger.error("OAuth2 사용자 이메일이 유효하지 않습니다.");
            throw new OAuth2AuthenticationException("invalid_oauth2_email");
        }

        // DB에서 이메일로 사용자 조회
        Users user = userService.getUser(email);
        if (user == null) {
            logger.warn("DB에서 일치하는 사용자를 찾을 수 없습니다. 이메일: {}", email);
            throw new OAuth2AuthenticationException("user_not_found");
        }

        // SecurityContextHolder에 사용자 정보 저장
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, // 사용자 객체
                null, // 비밀번호는 필요 없음
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())) // 권한
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        logger.info("사용자 인증 성공 및 SecurityContext에 저장 완료. 이메일: {}", email);

        // 이후 세션 직렬화/역직렬화 등으로 DefaultOAuth2User가 Principal이 될 수 있으므로, attributes는 그대로 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                attributes,
                userNameAttributeName
        );
    }

    /**
     * 네이버와 Google의 응답에서 이메일 추출
     */
    private String parseEmail(String registrationId, Map<String, Object> attributes) {
        if ("naver".equalsIgnoreCase(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response == null) {
                logger.error("Naver API 응답에서 'response' 객체가 누락되었습니다. 응답: {}", attributes);
                throw new OAuth2AuthenticationException("Naver API 응답에서 'response' 객체가 누락되었습니다.");
            }
            return (String) response.get("email"); // 네이버에서 이메일 추출 (response.email)
        } else if ("google".equalsIgnoreCase(registrationId)) {
            return (String) attributes.get("email"); // 구글에서 이메일 추출 (top-level email)
        }
        return null;
    }
}
