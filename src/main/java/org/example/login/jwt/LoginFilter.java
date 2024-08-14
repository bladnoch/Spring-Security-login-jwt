package org.example.login.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // 필터로 http 정보를 받아 확인 후 가공
    // 1. 들어온 정보를 바탕으로 authenticationManager에게 인증을 위한 토큰 생성후 반환
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //클라이언트 요청에서 username, password 추출
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        System.out.println("LoginFilter.attemptAuthentication");
        System.out.println("username = " + username);
        System.out.println("password = " + password);

        //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        // AuthenticationManager로 전달을 하기위해 UsernamePaswordAuthenticationToken으로 캡슐화
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        // token에 담은 검증을 위한 AuthenticationManager로 전달해서 인증 시도
        System.out.println("LoginFilter.attemptAuthentication");
        return authenticationManager.authenticate(authToken);
    }


    // access/refresh 를 위한 코드 v2ch4
    // 2(성공). AuthenticationManager로 보내진 토큰을 바탕으로 인증 성공시 Authentication 객체 생성 이를 바탕으로 토큰 생성
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        // 유저 정보
        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // 두가지의 토큰 생성 -> 생성엔 4개의 값이 필요 ch4v2
        String access = jwtUtil.createJwt("access", username, role, 600000L); //10분,
        String refresh = jwtUtil.createJwt("refresh", username, role, 36400000L); //24시간

        // 응답 설정 ch4v2
        response.setHeader("access", access); // header의 access key에다 넣어서 넘겨준다.
        response.addCookie(createCookie("refresh", refresh)); //
        response.setStatus(HttpStatus.OK.value());

    }

    // 로그인 실패시 실행하는 메소드
    // 2(실패).실패한 경우에는 완전히 인증된 Authentication 객체가 생성되지 않는다.
    // 대신, 실패 이유를 담은 AuthenticationException이 발생.
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
        System.out.println("잘못된 유저 또는 비밀번호 정보");

    }


    // refresh token을 담기위한 쿠키 생성 메소드 ch4v2
    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        // cookie.setSecure(true);  // https통신시 사용
        // cookeie.setPath("/");    // 쿠키가 적용될 범위
        cookie.setHttpOnly(true);

        return cookie;
    }

}
