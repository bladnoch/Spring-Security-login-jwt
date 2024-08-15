package org.example.login.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.login.dto.JoinDTO;
import org.example.login.service.JoinService;
import org.example.login.service.ReissueService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
@RequiredArgsConstructor
public class JoinController {
    private final JoinService joinService;
    private final ReissueService reissueService;

    /**
     * 회원가입을 진행하는 컨트롤러
     * @param joinDTO @username @password
     * @return String의 완료(ok) 사인 종료
     */
    @PostMapping("/join")
    public String joinProcess(@RequestBody JoinDTO joinDTO) {
        joinService.joinProcess(joinDTO);
        return "ok";
    }
}
