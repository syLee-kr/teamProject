package com.example.camping.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.camping.entity.Users;
import com.example.camping.userService.UserService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
	
	private UserService userService;
	
	// 프로필 조회
	@GetMapping("/profile")
	public String profile(@AuthenticationPrincipal User pricipal, Model model) {
		
		/*
		 * spirng security에서는 username = userId 자동으로 인식하기 때문에 
		 * 혼동방지를 위해 username 매개변수를 userId로 치환해서 사용
		 */  
		 
		// 로그인 한 사용자의 userId
		String userId = pricipal.getUsername();
		
	
		// 사용자 확인
		Users user = userService.findByUserId(userId);
		
		if (user == null) {
			return "redirect:/users/login/login";
		}
		
		model.addAttribute("user", user);
		
		return "users/profile/profile";
	}
	// 프로필 수정 폼
	@GetMapping("/edit")
	public String editProfile(@AuthenticationPrincipal User principal, Model model) {
		
		String userId = principal.getUsername();
		
		Users user = userService.findByUserId(userId);
		
		if (user == null) {
			return "redirect:/users/login/login";
		}
		
		model.addAttribute("user", user);
		
		return "users/profile/profile-edit";
	}
	
	// 프로필 수정 처리
	@PostMapping("/update")
	public String updateProfile(@ModelAttribute Users updatedUser,
								@AuthenticationPrincipal User principal) {
		String userId = principal.getUsername();
		
		// 사용자 업데이트
		Users user = userService.findByUserId(userId);
		
		if (user != null) {
			
			// 비밀번호 변경이 있을경우 암호화
			if (!updatedUser.getPassword().equals(user.getPassword())) {
				user.setPassword(updatedUser.getPassword());
				userService.registerUser(user);
			}
			
			// 비밀번호를 제외한 다른 정보 업데이트
			user.setName(updatedUser.getName());
			user.setPhone(updatedUser.getPhone());
			user.setEmail(updatedUser.getEmail());
			user.setAddress(updatedUser.getAddress());
			user.setBirthday(updatedUser.getBirthday());
			user.setGender(updatedUser.getGender());
			user.setProfileImage(updatedUser.getProfileImage());
			
			// 업데이트 된 정보 DB에 저장
			userService.save(user);
		}
		return "redirect:/users/profile/profile";

	}
	
}
