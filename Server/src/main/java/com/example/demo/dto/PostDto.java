package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostDto {
    private int postId;
    private String tattooistId;

    private String title;

    private String description;
    private int price;
    private int likeNum;
    private String genre;
    private String bigShape;
    private String smallShape;
    private String designUrl;

    private float avgCleanScore;

    private String url;  // image url


    @Override
    public String toString() {
        return "PostDto{" +
                "postId=" + postId +
                ", tattooistId=" + tattooistId +
                ", title=" + title +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", likeNum=" + likeNum +
                ", genre='" + genre + '\'' +
                ", bigShape='" + bigShape + '\'' +
                ", smallShape='" + smallShape + '\'' +
                ", designUrl=" + designUrl +
                ", avgCleanScore='" + avgCleanScore +
                ", url='" + url + '\'' +
                '}';
    }
}
