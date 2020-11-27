package com.example.newtattooandroid.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private String userId;
    private String userPw;
    private String name;
    private String nickName;
    private int sex; // 0 남성, 1 여성
    private int age;
    private String country;
    private String address;

    @Override
    public String toString() {
        return "UserDto [userId=" + userId + ", userPw=" + userPw + ", name=" + name + ", nick_name="
                + nickName + ", sex=" + sex + ", age=" + age + ", country=" + country + ", address=" + address + "]";
    }
}
