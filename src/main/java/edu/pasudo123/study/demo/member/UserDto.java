package edu.pasudo123.study.demo.member;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class UserDto {
    private String name;
    private int age;

    public UserDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
