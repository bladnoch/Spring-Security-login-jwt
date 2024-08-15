package org.example.login.controller;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.login.domain.RefreshEntity;
import org.example.login.jwt.JWTUtil;
import org.example.login.repository.RefreshRepository;
import org.example.login.service.ReissueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;


// ch6v2
//@Controller
//@ResponseBody
@RestController
@RequiredArgsConstructor
public class ReissueController {

    private final ReissueService reissueService;

    /*
        refresh 토큰을 이용해 access 토큰 재발급
        HttpServeltRequest를 통해 토큰이 담긴 쿠키를 전달받는다.
        access 토큰을 요청 하기 위해 refresh token 만 보면 되기 때문에 바디에 뭐가 들었건 딱히 상관 없음
     */
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        return reissueService.reissue(request, response);

    }


}


