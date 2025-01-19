package com.example.spring.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.spring.users.UsersService;
import com.example.spring.users.UsersVo;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsersService usersService;

    @Autowired
    private AuthService authService;

    // 회원가입
    @GetMapping("/register")
    public ModelAndView register() {
        return new ModelAndView("auth/register");
    }

    // 회원가입
    @PostMapping("/register")
    public ModelAndView register(@RequestBody UsersVo usersVo, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();

        boolean created = usersService.create(usersVo);

        if (created) {
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다.");
            mav.setViewName("redirect:/auth/login");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입에 실패했습니다.");
            mav.setViewName("redirect:/auth/register");
        }

        return mav;
    }

    // 로그인
    @GetMapping("/login")
    public ModelAndView login(@RequestParam(required = false) String error) {
        ModelAndView mav = new ModelAndView();
        if (error != null && error.equals("auth")) {
            mav.addObject("errorMessage", "로그인이 필요합니다.");
        }
        mav.setViewName("auth/login");
        return mav;
    }

    // 로그인
    @PostMapping("/login")
    public ModelAndView login(UsersVo usersVo, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();

        try {
            usersVo = authService.login(usersVo);

            if (usersVo != null) {
                // 세션 생성
                HttpSession session = request.getSession(true);
                session.setAttribute("user", usersVo);
                session.setAttribute("isLoggedIn", true);

                mav.setViewName("redirect:/auth/profile");
                return mav;
            }

            redirectAttributes.addFlashAttribute("errorMessage", "아이디 또는 비밀번호가 일치하지 않습니다.");
            mav.setViewName("redirect:/auth/login");

        } catch (Exception e) {
            e.printStackTrace();
            HttpSession session = request.getSession(false);
            session.invalidate();
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 처리 중 오류가 발생했습니다.");
            mav.setViewName("redirect:/auth/login");
        }

        return mav;
    }

    // 로그아웃
    @GetMapping("/logout")
    public ModelAndView logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return new ModelAndView("redirect:/auth/login");
    }

    // 프로필
    @GetMapping("/profile")
    public ModelAndView profile() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("auth/profile");
        return mav;
    }

    // 프로필 수정
    @GetMapping("/update-profile")
    public ModelAndView updateProfile() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("auth/update_profile");
        return mav;
    }

    // 프로필 수정
    @PostMapping("/update-profile")
    public ModelAndView updateProfile(UsersVo usersVo, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();

        boolean updated = usersService.update(usersVo);
        if (updated) {
            redirectAttributes.addFlashAttribute("successMessage", "프로필이 수정되었습니다.");
            mav.setViewName("redirect:/auth/profile");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "프로필 수정에 실패했습니다.");
            mav.setViewName("redirect:/auth/update-profile");
        }
        return mav;
    }

    // 비밀번호 수정
    @GetMapping("/update-password")
    public ModelAndView updatePassword() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("auth/update_password");
        return mav;
    }

    // 비밀번호 수정
    @PostMapping("/update-password")
    public ModelAndView updatePassword(
        @RequestParam("password") String password,
        @RequestParam("password1") String password1,
        @RequestParam("password2") String password2,
        HttpServletRequest request, 
        RedirectAttributes redirectAttributes
    ) {
        ModelAndView mav = new ModelAndView();

        // 사용자 아이디
        String userId = (String) request.getSession().getAttribute("userId");
        UsersVo existUsersVo = new UsersVo();
        existUsersVo.setUserId(userId);

        // 사용자 정보
        UsersVo existUser = usersService.read(existUsersVo);

        // 기존 비밀번호 확인
        if (!existUser.getPassword().equals(password)) {
            redirectAttributes.addFlashAttribute("errorMessage", "기존 비밀번호가 일치하지 않습니다.");
            mav.setViewName("redirect:/auth/update-password");
        } else {
            //새로운 사용자 정보
            UsersVo newPasswordUser = new UsersVo();
            newPasswordUser.setUserId(userId);
            newPasswordUser.setPassword(password1);

            // 비밀번호 수정
            boolean updated = usersService.updatePassword(newPasswordUser);
            if (updated) {
                redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 수정되었습니다.");
                mav.setViewName("redirect:/auth/profile");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "비밀번호 수정에 실패했습니다.");
                mav.setViewName("redirect:/auth/update-password");
            }
        }

        return mav;
    }

    // 아이디 찾기
    @GetMapping("/find-user-id")
    public ModelAndView findUserId() {
        return new ModelAndView("auth/find_user_id");
    }

    // 아이디 찾기
    @PostMapping("/find-user-id")
    public ModelAndView findUserId(UsersVo usersVo, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();

        UsersVo user = usersService.read(usersVo);        

        if (user != null) {
            redirectAttributes.addFlashAttribute("successMessage", "아이디는 " + user.getUserId() + "입니다.");
            mav.setViewName("redirect:/auth/find-user-id");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "아이디를 찾을 수 없습니다.");
            mav.setViewName("redirect:/auth/find-user-id");
        }

        return mav;
    }

    // 비밀번호 초기화
    @GetMapping("/reset-password")
    public ModelAndView resetPassword() {
        return new ModelAndView("auth/reset_password");
    }

    // 비밀번호 초기화
    @PostMapping("/reset-password")
    public ModelAndView resetPassword(UsersVo usersVo, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();

        UsersVo user = usersService.read(usersVo);        

        if (user != null) {
            String rndPassword = authService.resetPassword(usersVo);
            redirectAttributes.addFlashAttribute("successMessage", "초기화된 비밀번호는 " + rndPassword + "입니다.");
            mav.setViewName("redirect:/auth/reset-password");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "아이디를 찾을 수 없습니다.");
            mav.setViewName("redirect:/auth/reset-password");
        }

        return mav;
    }

    // 회원 탈퇴
    @PostMapping("/delete")
    public ModelAndView delete(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();

        // 사용자 정보
        String userId = (String) request.getSession().getAttribute("userId");
        UsersVo usersVo = new UsersVo();
        usersVo.setUserId(userId);

        boolean deleted = usersService.delete(usersVo);

        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "회원 탈퇴가 완료되었습니다.");
            mav.setViewName("redirect:/auth/logout");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "회원 탈퇴에 실패했습니다.");
            mav.setViewName("redirect:/auth/profile");
        }

        return mav;
    }
}
