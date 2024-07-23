package org.example.login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 검증할 때 캐시로 암호화 시켜서 검증, 진행 BCryptPasswordEncoder을 활
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // csrf disable
        http
                .csrf((auth) -> auth.disable());

        //Form 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        //http basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

        // admincontroller와 maincontroller의 인가 작업을 위한 코드
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login", "/", "join").permitAll() // 이 경로는 권안 허용
                        .requestMatchers("/admin").hasRole("ADMIN") // 어드민 경로는 어드민 권한을 가진 사람만 사용 가능
                        .anyRequest().authenticated()); // 나머지 다른 요청에 대해서는 로그인 한 사람만 가능

        //세션 설정(stateless)
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
