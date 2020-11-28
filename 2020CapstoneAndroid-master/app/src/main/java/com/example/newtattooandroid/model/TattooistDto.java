package com.example.newtattooandroid.model;

import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
//Todo : Tattoist profile 이미지, nickname 필요
public class TattooistDto {
    private String userId;
    private String nickName;
    private String bigAddress;
    private String smallAddress;
    private String mobile;
    private String description;

    public TattooistDto(){

    }

    public TattooistDto(HashMap<String, Object> parameters) {
        this.userId = (String) parameters.get("userId");
        this.nickName = (String) parameters.get("nickName");
        this.bigAddress = (String) parameters.get("bigAddress");
        this.smallAddress = (String) parameters.get("smallAddress");
        this.mobile= (String) parameters.get("mobile");
        this.description = (String) parameters.get("description");
    }

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
