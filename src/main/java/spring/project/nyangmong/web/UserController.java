package spring.project.nyangmong.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;
import spring.project.nyangmong.domain.pet.Pet;
import spring.project.nyangmong.domain.user.User;
import spring.project.nyangmong.handle.ex.CustomException;
import spring.project.nyangmong.service.PetService;
import spring.project.nyangmong.service.UserService;
import spring.project.nyangmong.util.RespScript;
import spring.project.nyangmong.util.Script;
import spring.project.nyangmong.util.UtilValid;
import spring.project.nyangmong.web.dto.members.user.IdFindReqDto;
import spring.project.nyangmong.web.dto.members.user.JoinDto;
import spring.project.nyangmong.web.dto.members.user.PwFindReqDto;

@RequiredArgsConstructor
@Controller
public class UserController {
    private final UserService userService;
    private final PetService petService;
    private final HttpSession session;
    private final HttpServletResponse response;

    // 회원 정보 수정 페이지
    @GetMapping("/s/user/{id}/update-form")
    public String userChangeForm(@PathVariable Integer id, Model model) {
        User principal = (User) session.getAttribute("principal");

        // 권한이 없을 때 - 로그인 후 다른 유저 페이지로 접근 시 스크립트 처리함
        if (principal.getId() != id) {
            String scriptMsg = Script.back("잘못된 접근입니다.");
            RespScript.스크립트로응답하기(scriptMsg, response);
        }

        User userEntity = userService.회원정보보기(id);
        Pet petEntity = petService.펫정보보기(id);
        model.addAttribute("user", userEntity);
        model.addAttribute("pet", petEntity);
        return "pages/user/userChange";
    }

    // 회원 정보 페이지
    @GetMapping("/s/user/{id}/detail")
    public String userDetail(@PathVariable Integer id, Model model) {
        User principal = (User) session.getAttribute("principal");

        // 권한이 없을 때 - 로그인 후 다른 유저 페이지로 접근 시 스크립트 처리함
        if (principal.getId() != id) {
            String scriptMsg = Script.back("잘못된 접근입니다.");
            RespScript.스크립트로응답하기(scriptMsg, response);
        }

        User userEntity = userService.회원정보보기(id);
        Pet petEntity = petService.펫정보보기(id);
        model.addAttribute("user", userEntity);
        model.addAttribute("pet", petEntity);
        return "pages/user/userDetail";
    }

    // 로그아웃하기
    @GetMapping("/logout")
    public String logout() {
        session.invalidate(); // 영역 전체를 날리는 것
        return "redirect:/";
    }

    // 로그인
    @PostMapping("/login")
    public String login(User user) {
        User userEntity = userService.로그인(user);
        session.setAttribute("principal", userEntity);

        // 아이디 또는 패스워드 불일치 시 스크립트 처리하는 로직
        if (userEntity == null) {
            String scriptMsg = Script.back("아이디 또는 패스워드가 일치하지 않습니다.");
            RespScript.스크립트로응답하기(scriptMsg, response);
        }

        // Remember me - userId 쿠키에 저장
        if (user.getRemember() != null && user.getRemember().equals("on")) {
            response.addHeader("Set-Cookie", "remember=" + user.getUserId());
        }
        return "redirect:/";
    }

    // 회원가입
    @PostMapping("/join")
    public String join(@Valid JoinDto joinDto, BindingResult bindingResult) {

        // validation
        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();
            for (FieldError fe : bindingResult.getFieldErrors()) {
                errorMap.put(fe.getField(), fe.getDefaultMessage());
            }
            // 유효성 검사해서 맞지 않을 시 스크립트 안내창 후 페이지 뒤로가기(데이터 남아있음)
            throw new CustomException(errorMap.toString());
        }

        userService.회원가입(joinDto.toEntity());
        return "redirect:/login-form";
    }

    // 회원가입 페이지
    @GetMapping("/join-form")
    public String joinForm() {
        return "pages/user/joinForm";
    }

    // 로그인 페이지
    @GetMapping("/login-form")
    public String loginForm(HttpServletRequest request, Model model) {
        // 쿠키로 아이디 기억하기
        if (request.getCookies() != null) {
            Cookie[] cookies = request.getCookies(); // JSessionID, remember 2개를 내부적으로 split 해주는 메서드
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("remember")) {
                    model.addAttribute("remember", cookie.getValue());
                }
            }
        }
        return "pages/user/loginForm";
    }

    // 회원가입 시 유저아이디 중복체크
    @GetMapping("/api/user/userid-same-check")
    public ResponseEntity<?> userIdSameCheck(String userId) {
        boolean isNotSame = userService.유저아이디중복체크(userId); // true (같지 않다)
        return new ResponseEntity<>(isNotSame, HttpStatus.OK);
    }

    // 회원가입 시 이메일 중복체크
    @GetMapping("/api/user/email-same-check")
    public ResponseEntity<?> emailSameCheck(String email) {
        boolean isNotSame = userService.이메일중복체크(email); // true (같지 않다)
        return new ResponseEntity<>(isNotSame, HttpStatus.OK);
    }

    // 추후 api <로그인을 인증해야해서> 옮겨야하는 맵핑
    // 유저가 즐겨찾기 한 장소 - 일단은 mapping만 해둔 상태
    @GetMapping("/s/user/{id}/favlist")
    public String likeList(@PathVariable Integer id, Model model) {
                User principal = (User) session.getAttribute("principal");
     if (principal.getId() != id) {
            String scriptMsg = Script.back("잘못된 접근입니다.");
            RespScript.스크립트로응답하기(scriptMsg, response);
        }

        return "pages/list/favoriteList";
    }

    // 유저가 마음에 들어한 댕냥이자랑 게시판 - 일단은 mapping만 해둔 상태.
    @GetMapping("/s/user/{id}/boardlike")
    public String boardLikeList(@PathVariable Integer id, Model model) {
                User principal = (User) session.getAttribute("principal");
     if (principal.getId() != id) {
            String scriptMsg = Script.back("잘못된 접근입니다.");
            RespScript.스크립트로응답하기(scriptMsg, response);
        }

        return "pages/list/likeList";
    }

    // 유저가 적은 댓글 보기 - 일단은 mapping만 해둔 상태
    @GetMapping("/s/user/{id}/commentlist")
    public String commentList(@PathVariable Integer id, Model model) {
                User principal = (User) session.getAttribute("principal");
     if (principal.getId() != id) {
            String scriptMsg = Script.back("잘못된 접근입니다.");
            RespScript.스크립트로응답하기(scriptMsg, response);
        }

        return "pages/list/commentlist";
    }

    // 아이디 찾기 페이지
    @GetMapping("/find/id-form")
    public String findIdForm() {
        return "pages/user/findIdForm";
    }

    // 아이디 찾기 요청
    @PostMapping("/find/id")
    public String idFind(@Valid IdFindReqDto idFindReqDto, BindingResult bindingResult, Model model) {
        UtilValid.요청에러처리(bindingResult);
        String findUserId = userService.아이디찾기(idFindReqDto);
        model.addAttribute("findUserId", findUserId);
        return "pages/user/showIdForm";
    }

    // 비밀번호 찾기 페이지
    @GetMapping("/find/pw-form")
    public String findPwForm() {
        return "pages/user/findPwForm";
    }

    // 비밀번호 찾기 요청
    @PostMapping("/find/pw")
    public String idFind(@Valid PwFindReqDto pwFindReqDto, BindingResult bindingResult, Model model) {
        UtilValid.요청에러처리(bindingResult);
        String findPassword = userService.패스워드찾기(pwFindReqDto);
        model.addAttribute("findPassword", findPassword);
        return "pages/user/showPwForm";
    }
}