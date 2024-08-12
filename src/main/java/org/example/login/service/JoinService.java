package org.example.login.service;

import lombok.RequiredArgsConstructor;
import org.example.login.domain.UserEntity;
import org.example.login.dto.JoinDTO;
import org.example.login.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

// 회원 등록을 위한 서비스
@Service
@RequiredArgsConstructor
public class JoinService {

    private final UserRepository userRepository;

    // SecurityConfig에서 @Bean 등록한 비밀번호 암호화를 위한 빈 사용
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 회원가입 시도시 호출
    public void joinProcess(JoinDTO joinDTO) {
        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();

        // 유저 이름 주복 확인
        Boolean isExist = userRepository.existsByUsername(username);
        if (isExist) {
            return;
        }

        // User 객체 생성 후 저장
        UserEntity data = new UserEntity();

        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setRole("ROLE_ADMIN");

        userRepository.save(data);
    }
}
