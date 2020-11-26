package com.example.newtattooandroid.model;

import lombok.*;

import java.util.ArrayList;

//Todo: 도안이미지 -> 상세페이지 response, Title 추가될 예정
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class MainItem {

    @NonNull private String bigShape;    //종류 - 대분류 ex) 장미, 호랑이, 용

    @NonNull private String title;

    @NonNull private String description; //세부페이지 설명글
    private String designUrl = null;   //도안 이미지
    @NonNull private String genre;       //장르. ex) 블랙앤그레이, 일러스트
    private int likenum;        //좋아요 수
    private float avgCleanScore;
    private int postId;         //게시물 ID
    private int price;          //가격
    @NonNull private String smallShape;  //소분류
    @NonNull private ArrayList<String> tattooUrl;    //타투 사진 urls
    @NonNull private String tattooistId; //타투이스트 ID

    @Override
    public String toString() {
        return "MainItem{" +
                "postId=" + postId +
                ", tattooistId=" + tattooistId +
                ", title=" + title +
                ", description='" + description +
                ", price=" + price +
                ", likenum=" + likenum +
                ", genre='" + genre +
                ", bigShape='" + bigShape +
                ", smallShape='" + smallShape +
                ", designUrl=" + designUrl +
                ", tattooUrl=" + tattooUrl +
                ", avgCleanScore=" + avgCleanScore +
                '}';
    }
}