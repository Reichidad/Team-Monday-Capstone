package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TattooImageDto {
    private int id;
    private int postId;
    private String url;
    private String fileName;

    @Override
    public String toString() {
        return "TattooImageDto{" +
                "id=" + id +
                ", postId=" + postId +
                ", url='" + url + '\'' +
                ", filename='" + fileName +
                '}';
    }
}
