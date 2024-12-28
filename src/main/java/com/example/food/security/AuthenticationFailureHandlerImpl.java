package com.example.food.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFailureHandlerImpl.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String loginPath = request.getRequestURI();
        logger.warn("로그인 실패: {}", exception.getMessage());

        String error = "invalid_credentials"; // 기본 에러 메시지
        if (exception.getMessage().contains("disabled")) {
            error = "account_disabled";
        } else if (exception.getMessage().contains("OAuth2")) {
            error = "oauth_error";
        }

        if (loginPath.startsWith("/admin/login")) {
            response.sendRedirect("/admin/login?error=" + error);
        } else if (loginPath.startsWith("/login")) {
            response.sendRedirect("/login?error=" + error);
        } else {
            response.sendRedirect("/login?error=unknown_error");
        }
    }
}

