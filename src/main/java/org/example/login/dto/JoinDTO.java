package org.example.login.dto;

import lombok.Getter;
import lombok.Setter;


// input으로 유저에게 받는 username, password를 가진다.
@Setter @Getter
public class JoinDTO {

    private String username;
    private String password;
}
