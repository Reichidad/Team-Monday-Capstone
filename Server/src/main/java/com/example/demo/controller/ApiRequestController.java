package com.example.demo.controller;

import com.example.demo.datatype.PostDetail;
import com.example.demo.service.ApiRequestService;
import com.example.demo.service.PostService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@RestController
public class ApiRequestController {

    private RestTemplate restTemplate;
    private ApiRequestService apiRequestService;
    private PostService postService;

    @PostMapping(value = "/test")
    public ArrayList<PostDetail> test(@RequestParam(value = "image") MultipartFile image) throws IOException {

//        byte[] imageBytes = image.getBytes();
        String response = apiRequestService.request(image);

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(response);
        JsonObject object = element.getAsJsonObject();

        JsonArray fileNameArray = object.get("filename_list").getAsJsonArray();
        List<String> fileNameList = new ArrayList<>();
        for (int idx = 0; idx < fileNameArray.size(); idx++) {
//            System.out.println(idx + " : " + postIdArray.get(idx).getAsInt());
            fileNameList.add(fileNameArray.get(idx).getAsString());
        }

        System.out.println(fileNameList);

        List<Integer> postIdList = new ArrayList<>();
        postIdList = postService.getPostIdList(fileNameList);
        System.out.println(postIdList);
        ArrayList<PostDetail> posts = postService.getSomePost(postIdList);

        return sortPostByFileName(fileNameList, posts);
    }

    private ArrayList<PostDetail> sortPostByFileName(List<String> fileNameList, ArrayList<PostDetail> posts) {
        ArrayList<PostDetail> sortedPosts = new ArrayList<>();

        boolean found = false;

        for (String fileName : fileNameList) {

            for (int idx = 0; idx < posts.size(); idx++) {
                PostDetail postDetail = posts.get(idx);
                ArrayList<String> urls = postDetail.getTattooUrl();

                for(String url : urls) {
                    if(url.contains(fileName)) {
                        sortedPosts.add(postDetail);
                        found = true;
                    }
                }
                if (found == true){
                    break;
                }
            }
            found = false;
        }

        return sortedPosts;
    }
}
