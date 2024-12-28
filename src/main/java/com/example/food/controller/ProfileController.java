package com.example.food.controller;

import com.example.food.entity.Users;
import com.example.food.repository.UserRepository;
import com.example.food.service.AuthenticationService;
import com.example.food.service.GcsUploadService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private static final String DEFAULT_PROFILE_IMAGE = "/images/profileimg.png";

    private final UserRepository userRepo;
    private final AuthenticationService authService;
    private final GcsUploadService gcsUploadService;

    @Autowired
    public ProfileController(UserRepository userRepo, AuthenticationService authService, GcsUploadService gcsUploadService) {
        this.userRepo = userRepo;
        this.authService = authService;
        this.gcsUploadService = gcsUploadService;
        logger.info("프로필 컨트롤러가 초기화되었습니다.");
    }

    // Enum 변환 설정
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Users.Gender.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(Users.Gender.fromCode(text));
            }
        });
    }

    private Users getLoggedInUser() {
        Users user = authService.getLoggedInUser();
        if (user == null) {
            logger.warn("사용자가 로그인하지 않았습니다.");
            return null;
        }
        return user;
    }

    @GetMapping
    public String profileMain(Model model) {
        logger.info("프로필 메인 페이지에 접근 중입니다.");

        Users user = getLoggedInUser();
        if (user == null) {
            return "redirect:/login";
        }

        prepareUserModel(user, model);
        return "user/profile/profile";
    }

    @GetMapping("/edit-profile")
    public String editProfile(Model model) {
        logger.info("프로필 수정 페이지에 접근 중입니다.");

        Users user = getLoggedInUser();
        if (user == null) {
            return "redirect:/login";
        }

        prepareUserModel(user, model);
        return "user/profile/edit-profile";
    }

    @PostMapping("/edit-profile")
    public String editProfileSubmit(@Valid @ModelAttribute("user") Users updatedUser,
                                    BindingResult result,
                                    @RequestParam(value = "profileImage", required = false) MultipartFile file,
                                    Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("errors", result.getAllErrors());

            Users user = getLoggedInUser();
            if (user != null) {
                prepareUserModel(user, model);
                logger.info("유효성 검증 실패 후 프로필 수정 페이지에 user 객체가 추가되었습니다: {}", user.getUserId());
            } else {
                logger.warn("로그인된 사용자가 없어 로그인 페이지로 리다이렉트합니다.");
                return "redirect:/login";
            }

            return "user/profile/edit-profile";
        }

        Users user = getLoggedInUser();
        if (user == null) {
            logger.warn("로그인된 사용자가 없어 로그인 페이지로 리다이렉트합니다.");
            return "redirect:/login";
        }

        updateUserFields(user, updatedUser, file);
        userRepo.save(user);
        logger.info("사용자 프로필이 성공적으로 업데이트되었습니다: {}", user.getUserId());

        return "redirect:/profile";
    }


    private void prepareUserModel(Users user, Model model) {
        setDefaultProfileImageIfEmpty(user);

        model.addAttribute("user", user);
        String formattedBirthday = user.getBirthday() != null
                ? user.getBirthday().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "정보 없음";
        model.addAttribute("formattedBirthday", formattedBirthday);
        logger.debug("Formatted Birthday: {}", formattedBirthday);

        String formattedRegDate = user.getRegdate() != null
                ? user.getRegdate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : "정보 없음";
        model.addAttribute("formattedRegDate", formattedRegDate);
        logger.debug("Formatted Registration Date: {}", formattedRegDate);
    }

    private void updateUserFields(Users user, Users updatedUser, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String fileUrl = gcsUploadService.uploadFile(file);
            user.setProfileImage(fileUrl);
            logger.info("프로필 이미지가 업데이트되었습니다: {}", fileUrl);
        }

        updateFieldIfChanged(updatedUser::getName, user::getName, value -> {
            user.setName(value);
            logger.info("이름이 업데이트되었습니다: {}", value);
        });

        updateFieldIfChanged(updatedUser::getPhone, user::getPhone, value -> {
            user.setPhone(value);
            logger.info("전화번호가 업데이트되었습니다: {}", value);
        });

        updateFieldIfChanged(updatedUser::getAddress, user::getAddress, value -> {
            user.setAddress(value);
            logger.info("주소가 업데이트되었습니다: {}", value);
        });

        updateFieldIfChanged(updatedUser::getBirthday, user::getBirthday, value -> {
            user.setBirthday(value);
            logger.info("생일이 업데이트되었습니다: {}", value);
        });

        updateFieldIfChanged(updatedUser::getGender, user::getGender, value -> {
            user.setGender(value);
            logger.info("성별이 업데이트되었습니다: {}", value);
        });

        setDefaultProfileImageIfEmpty(user);
    }

    private <T> void updateFieldIfChanged(Supplier<T> newValueSupplier, Supplier<T> currentValueSupplier, Consumer<T> updater) {
        T newValue = newValueSupplier.get();
        T currentValue = currentValueSupplier.get();
        if (newValue != null && !newValue.equals(currentValue)) {
            updater.accept(newValue);
        }
    }

    private void setDefaultProfileImageIfEmpty(Users user) {
        if (user.getProfileImage() == null || user.getProfileImage().trim().isEmpty()) {
            user.setProfileImage(DEFAULT_PROFILE_IMAGE);
            logger.info("기본 프로필 이미지가 설정되었습니다. 사용자 ID: {}", user.getUserId());
        }
    }
}
