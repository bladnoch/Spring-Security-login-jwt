package org.example.login.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.login.domain.UserEntity;
import org.example.login.dto.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

// OncePerRequestFilter: 요청에 대해 한번만 진행
// client 한테 받은 토큰을 확인
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;



    /*
        OncePerRequestFilter을 사용하기 위한 필수 함수
        토큰 관리
        access/refresh token을 사용하면서 함수 내부 전부 바뀜 이전 코드는 주석 ch5v2
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 헤더에서 access키에 담긴 토큰을 꺼냄
        String accessToken = request.getHeader("access");

        // 토큰이 없다면 다음 필터로 넘김
        // 권한이 필요 없는 경우도 있기 때문에 다음 필터로 넘긴다.
        if (accessToken == null) {

            filterChain.doFilter(request, response);

            return;
        }

        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        // header에 access로 보낼 수는 있지만 실제 토큰을 까봤을 때 access 토큰이 아닐 수 있기 때문에 확인을 한다.
        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("access")) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // token 이 가지고 있던 username, role 값을 획득
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setRole(role);
        // password는 넣지 않았기 때문에 null, 애초에 토큰 생성에 필요하지 않기는 함

        // UserEntity 객체를 CustomUserDetails라는 사용자 정의 UserDetails 객체로 변환합니다.
        // CustomUserDetails는 Spring Security에서 사용자 인증을 처리하기 위해 사용되는 객체입니다.
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

        // Authentication 객체를 생성하여, 사용자의 인증 정보를 담습니다.
        // 여기서는 UsernamePasswordAuthenticationToken을 사용하여 인증 토큰을 생성하고,
        // 이 토큰에는 사용자 정보(customUserDetails)와 권한(getAuthorities())이 포함됩니다.
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        // 이 부분이 중요한데, SecurityContextHolder를 통해 SecurityContext에 인증 정보를 저장합니다.
        // 이로 인해 이후의 요청들에서 이 사용자는 인증된 사용자로 간주됩니다.
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);

        // token 1개만 사용할 때 사용하던 코드 acc/ref token 사용하면서 더이상 사용하지 않음
//        // request에서 Authorization 헤더를 찾음
//        String authorization = request.getHeader("Authorization");
//
//        // Authorization 헤더 검증
//        // 토큰 없을경우 실행
//        if (authorization == null || !authorization.startsWith("Bearer ")) {
//            System.out.println("token null");
//            filterChain.doFilter(request, response);
//
//            // 조검이 해당되면 매소드 종료(필수)
//            return;
//        }
//
//        System.out.println("authorization now");
//
//        //Bearer 부분 제거 후 순수 토큰만 획득
//        String token = authorization.split(" ")[1];
//
//        // 토큰 소멸 시간 검증
//        // 소멸 시간 지났을 경우 실행
//        if (jwtUtil.isExpired(token)) {
//
//            System.out.println("token expired");
//            filterChain.doFilter(request, response);
//
//            // 조건이 해당되면 메소드 종료(필수)
//            return;
//        }
//
//        // DB를 매번 조회하지 않고 토큰 정보를 제활용
//        // 토큰에서 username과 role 획득
//        String username = jwtUtil.getUsername(token);
//        String role = jwtUtil.getRole(token);
//
//        // userEntity를 생성하여 값 set
//        UserEntity userEntity = new UserEntity();
//        userEntity.setUsername(username);
//        /*
//            한번만 사용되고 소멸되기 때문에 임시의 비밀번호를 넣어 userEntity를 생성
//            token엔 비밀번호 정보가 없고(username, role)
//            만약 매번 생성때마다 db를 조회할 경우 효율이 좋지 않기 때문에
//            임시값을 넣어 토큰을 생성
//         */
//        userEntity.setPassword("temppassword");
//        userEntity.setRole(role);
//
//        // userEntity에 회원 정보 객체 담기
//        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
//
//        // 스프링 시큐리티 인증 토큰 생성
//        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
//
//        // 새션에 사용자 등록
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//        filterChain.doFilter(request, response);
    }

}
