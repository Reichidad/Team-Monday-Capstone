package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TattooistDto {
    private String userId;
    private String nickName;
    private String bigAddress;
    private String smallAddress;
    private String mobile;
    private String description;

    @Override
    public String toString() {
        return "TattooistDto{" +
                "userId='" + userId + '\'' +
                "nickName='" + nickName + '\'' +
                ", bigAddress='" + bigAddress + '\'' +
                ", smallAddress='" + smallAddress + '\'' +
                ", mobile='" + mobile + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
