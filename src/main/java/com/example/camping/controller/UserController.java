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

import com.example.camping.domain.Users;
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
		
		// 로그인 한 사용자의 username
		String username = pricipal.getUsername();
		
		// 사용자 확인
		Users user = userService.findByUsername(username);
		
		if (user == null) {
			return "redirect:/login";
		}
		
		model.addAttribute("user", user);
		
		return "user/profile";
	}
	// 프로필 수정 폼
	@GetMapping("/edit")
	public String editProfile(@AuthenticationPrincipal User principal, Model model) {
		
		String username = principal.getUsername();
		
		Users user = userService.findByUsername(username);
		
		if (user == null) {
			return "redirect:/login";
		}
		
		model.addAttribute("user", user);
		
		return "user/edit";
	}
	
	// 프로필 수정 처리
	@PostMapping("/update")
	public String updateProfile(@ModelAttribute Users updatedUser,
								@AuthenticationPrincipal User principal) {
		String username = principal.getUsername();
		
		// 사용자 업데이트
		Users user = userService.findByUsername(username);
		
		if (user != null) {
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
		return "redirect:/user/profile";

	}
	// 비밀번호 변경 폼
	@GetMapping("/change-password")
	public String changePasswordForm() {
		return "user/change-password";
	}
	
	// 비밀번호 변경 처리
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String oldPassword,
								 @RequestParam String newPassword,
								 @AuthenticationPrincipal User principal) {
		
		String username = principal.getUsername();
		
		Users user = userService.findByUsername(username);
		
		// 기존 비밀번호가 일치하면 비밀번호 변경
		if(user != null && user.getPassword().equals(oldPassword)) {
			user.setPassword(newPassword);
			userService.save(user);
		}else {
			return "user/change-password";
		}
		
		return "redirect:/user/profile";
		
	}
	
	
}
