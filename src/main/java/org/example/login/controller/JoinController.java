package org.example.login.controller;

import lombok.RequiredArgsConstructor;
import org.example.login.dto.JoinDTO;
import org.example.login.service.JoinService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;


    /**
     * 회원가입을 진행하는 컨트롤러
     * @param joinDTO @username @password
     * @return String의 완료(ok) 사인 종료
     */
    @PostMapping("/join")
    public String joinProcess(JoinDTO joinDTO) {
        joinService.joinProcess(joinDTO);
        return "ok";
    }


}
