package com.example.camping.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.camping.entity.Users;
import com.example.camping.userService.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/")
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
			return "redirect:/users/login/login-form";
		}
		
		model.addAttribute("user", user);
		
		return "users/profile/profile-form";
	}
	// 프로필 수정 폼
	@GetMapping("/edit")
	public String editProfile(@AuthenticationPrincipal User principal, Model model) {
		
		String userId = principal.getUsername();
		
		Users user = userService.findByUserId(userId);
		
		if (user == null) {
			return "redirect:/users/login/login-form";
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
		return "redirect:/users/profile/profile-form";

	}
	// 비밀번호 변경 폼
	@GetMapping("/change-password")
	public String changePasswordForm() {
		return "users/profile/change-password";
	}
	
	// 비밀번호 변경 처리
	@PostMapping("/change-password")
	public String changePassword(@RequestParam(name="oldPassword") String oldPassword,
								 @RequestParam(name="newPassword") String newPassword,
								 @AuthenticationPrincipal User principal,
								 Model model,
								 HttpServletRequest request, HttpServletResponse response) {
		
		String userId = principal.getUsername();
		log.info("비밀번호 변경 요청: 사용자 {}", userId);
		
		Users user = userService.findByUserId(userId);
		if(user == null) {
			log.warn("사용자를 찾을 수 없음: {}", userId);
			model.addAttribute("error", "사용자를 찾을수 없습니다.");
			return "users/profile/change-password";
		}

		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		
		
		// 기존 비밀번호가 일치하면 비밀번호 변경
		if(user != null && passwordEncoder.matches(oldPassword, user.getPassword())) {
			// 비밀번호 암호화 후 저장
			user.setPassword(passwordEncoder.encode(newPassword));
			userService.save(user);
			log.info("비밀번호 변경 완료: 사용자 {}", userId);
			
			// 비밀번호 변경 후 로그아웃 처리
			SecurityContextHolder.clearContext();
			
			// 로그아웃 처리(세션 무효)
			LogoutHandler logoutHandler = new SecurityContextLogoutHandler();
			logoutHandler.logout(request, response, 
					SecurityContextHolder.getContext().getAuthentication());
			log.info("로그아웃 처리 완료: 사용자 {}", userId);
			
			// 메세지 전달
			String msg = "비밀번호가 변경되었습니다. 다시 로그인해주세요.";
			model.addAttribute("msg", msg);
			
			return "users/login/login-form";
		}else {
			log.warn("기존 비밀번호가 맞지 않음: 사용자 {}", userId);
			model.addAttribute("error", "기존 비밀번호가 맞지 않습니다.");
			return "users/profile/change-password";
		}
		
	}
	

	// 회원 탈퇴 처리   --- 마지막 반값이 이상함
	@PostMapping("/del-account")
	public String deleteAccount(@AuthenticationPrincipal User principal, Model model) {
		String userId = principal.getUsername();
		Users user = userService.findByUserId(userId);
		
		if (user != null) {
			// 사용자 삭제
			userService.delete(user);
			model.addAttribute("message", "회원 탈퇴가 완료되었습니다.");
			return "redirect:/users/login/login-form"; // 탈퇴 후 로그인 페이지로 리디렉션
		}
		return "redirect:/users/profile";
	}
	
}
