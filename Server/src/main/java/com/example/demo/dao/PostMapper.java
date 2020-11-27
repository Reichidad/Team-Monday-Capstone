package com.example.demo.dao;

import com.example.demo.dto.PostDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface PostMapper {

    public ArrayList<PostDto> selectAllPost();
    public ArrayList<PostDto> selectSomePost(Map<String, Object> map);
    public ArrayList<PostDto> selectPostByTattooistId(String tattooistId);
    public void insertPost(PostDto postDto);
    public void deletePost(int postId);

    public PostDto selectPreCleanScore(int postId);
    public void updateCleanScore(@Param("postId") int postId, @Param("avgCleanScore") float avgCleanScore);

    public ArrayList<PostDto> searchPostId(PostDto postDto);

    // TODO
//    public PostDto selectOnePost (int postId);
//    public ArrayList<PostDto> selectOnePostAllImages(int postId);

}
