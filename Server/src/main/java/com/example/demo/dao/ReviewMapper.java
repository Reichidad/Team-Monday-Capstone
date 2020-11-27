package com.example.demo.dao;

import com.example.demo.dto.PostDto;
import com.example.demo.dto.ReviewDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
@Mapper
public interface ReviewMapper {

    public ArrayList<ReviewDto> selectAllReviewOfOnePost(int postId);
    public ArrayList<ReviewDto> selectAllUserReviews(String userId);
    public void insertReview(ReviewDto reviewDto);
    public void deleteReview(int reviewId);

    public ArrayList<ReviewDto> getReviewCount(int postId);

}
