package edu.pasudo123.study.demo.member;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class MemberDto {

    private String username;
    private int age;

    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
