package com.example.demo.service;

import com.example.demo.dao.PostMapper;
import com.example.demo.dao.ReviewMapper;
import com.example.demo.dto.ReviewDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@AllArgsConstructor
@Service
public class ReviewService {
    private ReviewMapper reviewMapper;

    private PostMapper postMapper;

    public ArrayList<ReviewDto> getAllReviewOfOnePost(int postId){
        ArrayList<ReviewDto> reviewDtos = reviewMapper.selectAllReviewOfOnePost(postId);
        return reviewDtos;
    }

    public ArrayList<ReviewDto> getAllUserReviews(String userId){
        ArrayList<ReviewDto> reviewDtos = reviewMapper.selectAllUserReviews(userId);
        return reviewDtos;
    }

    public void writeReview(ReviewDto reviewDto){
        ArrayList<ReviewDto> rs = reviewMapper.getReviewCount(reviewDto.getPostId());
        int postReviewCount = 0;
        for(ReviewDto r : rs){
            if (r != null)
                postReviewCount += 1;
        }
        float postPreScore = postMapper.selectPreCleanScore(reviewDto.getPostId()).getAvgCleanScore();

        int newInputScore = reviewDto.getCleanScore();

        float all = postPreScore * postReviewCount + newInputScore;
        float newAverage = all / (postReviewCount + 1);

//        System.out.println("PrePostCount : " + (postReviewCount) + ", PreScore : " + postPreScore + ", newAverage : " + newAverage);

        reviewMapper.insertReview(reviewDto);
        postMapper.updateCleanScore(reviewDto.getPostId(), newAverage);
    }

    public void deleteReview(int reviewId){
        reviewMapper.deleteReview(reviewId);
    }
}
