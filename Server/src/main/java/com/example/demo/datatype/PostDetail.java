package com.example.demo.datatype;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/*
    All information of One Detail Post
 */
@Getter
@Setter
public class PostDetail {
    private int postId;
    private String tattooistId;
//    private String profileUrl;
    private String title;
    private String description;
    private int price;
    private int likeNum;
    private String genre;
    private String bigShape;
    private String smallShape;
    private String designUrl;

    private ArrayList<String> tattooUrl; // tattoo images
    private float avgCleanScore;
    @Override
    public String toString() {
        return "PostDetail{" +
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
                ", tattooUrl=" + tattooUrl +
                ", avgCleanScore=" + avgCleanScore +
                '}';
    }
}
