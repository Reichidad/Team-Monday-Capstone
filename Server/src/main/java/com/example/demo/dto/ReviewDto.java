package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {
    private int reviewId;
    private String userId;
    private int postId;
    private String nickName;
    private String date;
    private String description;
    private int cleanScore;
    private String tattooUrl1;
    private String tattooUrl2;
    private String cleanUrl;

    @Override
    public String toString() {
        return "ReviewDto{" +
                "reviewId=" + reviewId +
                ", userId='" + userId + '\'' +
                ", post_id=" + postId +
                ", nickName='" + nickName + '\'' +
                ", date='" + date + '\'' +
                ", description='" + description + '\'' +
                ", cleanScore=" + cleanScore +
                ", tattooUrl1='" + tattooUrl1 + '\'' +
                ", tattooUrl2='" + tattooUrl2 + '\'' +
                ", cleanUrl='" + cleanUrl + '\'' +
                '}';
    }
}
