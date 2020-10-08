package edu.pasudo123.study.demo.member;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AccountDto {

    private String username;
    private int age;

    @QueryProjection
    public AccountDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
