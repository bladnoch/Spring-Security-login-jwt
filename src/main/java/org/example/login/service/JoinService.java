package org.example.login.service;

import lombok.RequiredArgsConstructor;
import org.example.login.domain.UserEntity;
import org.example.login.dto.JoinDTO;
import org.example.login.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final UserRepository userRepository;

    // SecurityConfig에서 @Bean 등록한 비밀번호 암호화를 위한 빈 사용
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    // lombok을 사용하기 때문에 초기화 할 필요가 없지만 일단 추가
//    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
//        this.userRepository = userRepository;
//        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
//    }

    public void joinProcess(JoinDTO joinDTO) {
        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();

        Boolean isExist = userRepository.existsByUsername(username);
        if (isExist) {
            return;
        }

        UserEntity data = new UserEntity();

        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setRole("ROLE_ADMIN");

        userRepository.save(data);
    }
}
