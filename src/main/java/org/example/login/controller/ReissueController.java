package org.example.login.controller;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.login.domain.RefreshEntity;
import org.example.login.jwt.JWTUtil;
import org.example.login.repository.RefreshRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;


// ch6v2
 @Controller
 @ResponseBody
//@RestController
@RequiredArgsConstructor
public class ReissueController {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    // refresh 토큰을 이용해 access 토큰 재발급
    // HttpServeltRequest를 통해 토큰이 담긴 쿠키를 전달받는다.
    // access 토큰을 요청 하기 위해 refresh token 만 보면 되기 때문에 바디에 뭐가 들었건 딱히 상관 없음
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        // 중요: 아래의 코드는 추후 서비스 단으로 리팩토링 해야함
        // 쿠키에서 refresh token 을 가져온다
        String refresh = null;

        // 리프레시 토큰이 있는지 확인하는 코드 필요
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refresh")) {

                refresh = cookie.getValue();
            }
        }


        // 리프레시 코드 없음
        // 응답 어떻게 보낼지 상의 필요
        if (refresh == null) {

            //response status code
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        //expired check
        // 응답 어떻게 보낼지 상의 필요
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            //response status code
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {

            //response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        // DB에 저장되어 있는지 확인
        // 없으면 응답코드
        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }


        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L); // 24시간

        // refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(username, newRefresh, 86400000L);

        //response
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh",newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // ch8v2
    // refresh token을 DB에 저장
    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        // cookie.setSecure(true);  // https통신시 사용
        // cookeie.setPath("/");    // 쿠키가 적용될 범위
        cookie.setHttpOnly(true);

        return cookie;
    }
}


