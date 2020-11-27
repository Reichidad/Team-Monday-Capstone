package com.example.demo.controller;

import com.example.demo.datatype.PostDetail;
import com.example.demo.dto.UserDto;
import com.example.demo.service.PostService;
import com.example.demo.service.UserService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@Controller
@AllArgsConstructor
public class DemoController {
    private UserService userService;
    private PostService postService;

    @GetMapping("/db")
    public String dbTest(){
        UserDto dto = userService.getUser("donghae0414@naver.com");
        System.out.println(dto.toString());

        ArrayList<PostDetail> posts = postService.getAllPost();
        System.out.println("post size : " + posts.size());
        for(PostDetail post : posts){
            System.out.println(post.toString());
        }

        return "1.html";
    }

    @GetMapping("/db1")
    public String dbTest2(){
        // test Write Post
        PostDetail postDetail = new PostDetail();
        postDetail.setTattooistId("ddd@google.com");
        postDetail.setDescription("in Spring Test");
        postDetail.setGenre("Spring");
        postDetail.setBigShape("Flower");
        postDetail.setSmallShape("rose");
        postDetail.setDesignUrl(null);
        ArrayList<String> tattooUrl = new ArrayList<>();
        tattooUrl.add("https://storage.cloud.google.com/capstone-image-bucket/tattoo.jpg");
        tattooUrl.add("https://storage.cloud.google.com/capstone-image-bucket/place.jpg");
        postDetail.setTattooUrl(tattooUrl);
        postService.writePost(postDetail);

        return "1.html";
    }

    @GetMapping("/hello")
    public String list(Model model) throws IOException {
        System.out.println("123");

        //객체 다운로드
//        Storage storage = StorageOptions.getDefaultInstance().getService();
//        Blob blob = storage.get(BlobId.of("capstone-image-bucket", "blog.jpg"));
//        blob.downloadTo(Paths.get("C:\\Users\\dongh\\IdeaProjects\\demo\\download\\blog.jpg"));

        //객체 업로드
        StorageOptions storageOptions = StorageOptions.newBuilder()
                .setProjectId("YOUR_PROJECT_ID")
                .setCredentials(GoogleCredentials.fromStream(new
                        FileInputStream("src\\main\\resources\\capstone-274707-8dc2e791e977.json"))).build();
        Storage storage = storageOptions.getService();

        BlobInfo blobInfo =storage.create(
                BlobInfo.newBuilder("capstone-image-bucket", "tattoo1.jpg")
                        .setAcl(new ArrayList<>(Arrays.asList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
                        .build(),
                new FileInputStream(new File("download/dongwuk.jpg")));

        model.addAttribute("name", "https://storage.googleapis.com/capstone-image-bucket/design.jpg");
        return "show.html";
    }
}
