package com.example.camping.service;

import com.example.camping.domain.Users;
import com.example.camping.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByUserId(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with userId: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),
                user.getPassword(),
                Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                user.getRole().equals(Users.Role.ROLE_ADMIN) ? "ROLE_ADMIN" : "ROLE_USER"
                        )
                )
        );
    }
}

