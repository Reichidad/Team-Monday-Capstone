package com.example.demo.service;

import com.example.demo.dao.PostMapper;
import com.example.demo.dao.TattooImageMapper;
import com.example.demo.datatype.PostDetail;
import com.example.demo.dto.PostDto;
import com.example.demo.dto.TattooImageDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class PostService {
    private PostMapper postMapper;
    private TattooImageMapper tattooImageMapper;

    public void writePost(PostDetail postDetail) {
        System.out.println("writePost");
        PostDto postDto = postDetailToDto(postDetail);
        postMapper.insertPost(postDto);
        System.out.println("insert : " + postDto.toString());

        ArrayList<String> tattooUrl = postDetail.getTattooUrl();
        ArrayList<PostDto> list = postMapper.searchPostId(postDto);
        int postId = list.get(0).getPostId();
//        System.out.println("postID : " + postId);

        for (int idx = 0; idx < tattooUrl.size(); idx++) {
            TattooImageDto tattooImageDto = new TattooImageDto();
            tattooImageDto.setPostId(postId);
            tattooImageDto.setUrl(tattooUrl.get(idx));
            String filename = tattooUrl.get(idx).substring(60, tattooUrl.get(idx).length());
            System.out.println("url : " + tattooUrl.get(idx));
            System.out.println(filename);
            tattooImageDto.setFileName(filename);
            tattooImageMapper.insertTattooImage(tattooImageDto);

//            System.out.println("insert : " + tattooImageDto.toString());
        }
    }

    public ArrayList<PostDetail> getAllPost() {
        ArrayList<PostDetail> postDetails = new ArrayList<>();

        ArrayList<PostDto> postDtos = postMapper.selectAllPost();

        postDetails = postDtosToDetails(postDtos);

        return postDetails;
    }

    public ArrayList<PostDetail> getPostByTattooist(String tattooistId){
        ArrayList<PostDetail> postDetails = new ArrayList<>();

        ArrayList<PostDto> postDtos = postMapper.selectPostByTattooistId(tattooistId);

        postDetails = postDtosToDetails(postDtos);

        return postDetails;
    }

    public ArrayList<PostDetail> getSomePost(List<Integer> postIdList) {
        ArrayList<PostDetail> postDetails = new ArrayList<>();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("postIdList", postIdList);
        ArrayList<PostDto> postDtos = postMapper.selectSomePost(map);

        postDetails = postDtosToDetails(postDtos);

        return postDetails;
    }

    public void deleteAPost(int postId){
        postMapper.deletePost(postId);
    }


    /*
        get filename list
        return distinct post id list
     */
    public ArrayList<Integer> getPostIdList(List<String> fileNameList){
        ArrayList<TattooImageDto> tattooImageDtos = new ArrayList<>();
        ArrayList<Integer> postIdList = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("fileNameList", fileNameList);
        tattooImageDtos = tattooImageMapper.searchPostIdList(map);

        for(TattooImageDto id : tattooImageDtos){
            postIdList.add(id.getPostId());
        }

        return postIdList;
    }

    /*
        postDtos to postDetails
        use in getAllPost
     */
    private ArrayList<PostDetail> postDtosToDetails(ArrayList<PostDto> postDtos){
        ArrayList<PostDetail> postDetails = new ArrayList<>();

        PostDto nowDto = new PostDto();
        nowDto.setPostId(-1);
        PostDetail nowDetail = new PostDetail();

        for (PostDto postDto : postDtos) {
//            System.out.println(postDto.toString());

            if (nowDto.getPostId() != postDto.getPostId()) { // new Post
                nowDto = postDto;
                nowDetail = new PostDetail();
                nowDetail.setTattooUrl(new ArrayList<String>());

                postDtoToDetail(nowDto, nowDetail);

                postDetails.add(nowDetail);
                continue;
            }

            if (postDto.getUrl() != null) {
                nowDetail.getTattooUrl().add(postDto.getUrl());
            }
        }

        return postDetails;
    }

    /*
        use in postDtosToDetails
     */
    private void postDtoToDetail(PostDto dto, PostDetail detail) {
        detail.setPostId(dto.getPostId());
        detail.setTattooistId(dto.getTattooistId());
        detail.setTitle(dto.getTitle());
        detail.setDescription(dto.getDescription());
        detail.setPrice(dto.getPrice());
        detail.setLikeNum(dto.getLikeNum());
        detail.setGenre(dto.getGenre());
        detail.setBigShape(dto.getBigShape());
        detail.setSmallShape(dto.getSmallShape());
        detail.setDesignUrl(dto.getDesignUrl());
        detail.setAvgCleanScore(dto.getAvgCleanScore());
        if (dto.getUrl() != null)
            detail.getTattooUrl().add(dto.getUrl());
    }

    /*
        PostDetail to PostDto
        use in writePost
     */
    private PostDto postDetailToDto(PostDetail postDetail) {

        PostDto postDto = new PostDto();
        postDto.setTattooistId(postDetail.getTattooistId());
        postDto.setTitle(postDetail.getTitle());
        postDto.setDescription(postDetail.getDescription());
        postDto.setPrice(postDetail.getPrice());
        postDto.setLikeNum(0);
        postDto.setGenre(postDetail.getGenre());
        postDto.setBigShape(postDetail.getBigShape());
        postDto.setSmallShape(postDetail.getSmallShape());
        postDto.setDesignUrl(postDetail.getDesignUrl());
        postDto.setAvgCleanScore(0);

        return postDto;
    }
}
