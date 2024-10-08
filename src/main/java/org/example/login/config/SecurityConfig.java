package org.example.login.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.login.jwt.CustomLogoutFilter;
import org.example.login.jwt.JWTFilter;
import org.example.login.jwt.JWTUtil;
import org.example.login.jwt.LoginFilter;
import org.example.login.repository.RefreshRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    // 검증할 때 캐시로 암호화 시켜서 검증, 진행 BCryptPasswordEncoder을 활용
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        /**
         * 클라이언트와의 cors 설정
         */
        http
                .cors((cors) -> cors
                        .configurationSource(new CorsConfigurationSource() {


                            @Override
                            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                                CorsConfiguration configuration = new CorsConfiguration();

                                configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); // 3000번대 포트 사용가능
                                configuration.setAllowedMethods(Collections.singletonList("*")); // get,post 등 모든 옵션 가능
                                configuration.setAllowCredentials(true); // client-side에서 크리덴셜 설정을 하면 true로 설정
                                configuration.setExposedHeaders(Collections.singletonList("*"));
                                configuration.setMaxAge(3600L);

                                configuration.setExposedHeaders(Collections.singletonList("Authorization")); // jwt를 넣을 auth를 허용
                                return configuration;
                            }
                        }));

        // csrf disable
        /**
         * CSRF: CSRF 공격은 사용자가 인증된 세션을 통해 악의적인 요청을 보내는 공격입니다. Spring Security는 기본적으로 CSRF 보호를 활성화합니다. -> disable
         * 실제 서비스 환경에서도 CSRF는 disable하는 경우가 많음
         */
        http
                .csrf((auth) -> auth.disable());

        /**
         * Form 로그인 방식 disable
         * 토큰 기반 인증이나 OAuth2 등을 사용할 때 Form 로그인을 비활성화할 수 있습니다.
         */
        http
                .formLogin((auth) -> auth.disable());

        //http basic 인증 방식 disable
        /**
         * HTTP Basic 인증: HTTP Basic 인증은 사용자 이름과 비밀번호를 Base64로 인코딩하여 HTTP 헤더에 포함시키는 방식입니다. 간단하지만 보안에 취약할 수 있습니다. -> disable
         */
        http
                .httpBasic((auth) -> auth.disable());

        // admincontroller와 maincontroller의 인가 작업을 위한 코드
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login", "/", "join").permitAll() // 이 경로는 권안 허용
                        .requestMatchers("/admin").hasRole("ADMIN") // 어드민 경로는 어드민 권한을 가진 사람만 사용 가능
                        .requestMatchers("/reissue").permitAll() // access가 만료되어 로그인이 안되어있는 상태기 때문에 permitAll()
                        .anyRequest().authenticated()); // 나머지 다른 요청에 대해서는 로그인 한 사람만 가능

        // OAuth2 적용시 로그인에서 무한 루프가 일어날 경우 .addFilterBefore() -> .addFilterAfter()
        // JWTFilter(),  JWTUtil 사용
        http
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

        // 필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함) 따라서 등록 필요
        // LoginFilter(), JWTUtil
        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository), UsernamePasswordAuthenticationFilter.class);

        // logout filter 추가
        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);

        //세션 설정(stateless)
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}
