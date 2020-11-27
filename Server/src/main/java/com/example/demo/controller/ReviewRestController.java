package com.example.demo.controller;

import com.example.demo.dto.ReviewDto;
import com.example.demo.service.ReviewService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@org.springframework.web.bind.annotation.RestController
@AllArgsConstructor
public class ReviewRestController {
    private ReviewService reviewService;

    @GetMapping("/review")
    public ArrayList<ReviewDto> getReviews(@RequestParam(value = "postId") int postId) {
        System.out.println("get review request");
        return reviewService.getAllReviewOfOnePost(postId);
    }

    @GetMapping("/user/review")
    public ArrayList<ReviewDto> getUserReviews(@RequestParam(value = "userId") String userId){
        System.out.println("get " + userId + " user review request");
        return reviewService.getAllUserReviews(userId);
    }

    //    @PostMapping(value = "/write/review", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public void writeReview(@RequestBody ReviewDto reviewDto){
    @PostMapping(value = "/write/review", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public void writeReview(@RequestPart String review,
                            @RequestParam(value = "tattoos", required = false) List<MultipartFile> tattoos,
                            @RequestParam(value = "clean", required = false) MultipartFile clean) throws IOException {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(review);
        JsonObject object = element.getAsJsonObject();
        ReviewDto reviewDto = new ReviewDto();

        reviewDto.setUserId(object.get("userId").getAsString());
        reviewDto.setPostId(object.get("postId").getAsInt());
        reviewDto.setNickName(object.get("nickName").getAsString());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime dt = LocalDateTime.now();
        String dtString = dt.format(dtf);

        reviewDto.setDate(dtString);
        reviewDto.setDescription(object.get("description").getAsString());
        reviewDto.setCleanScore(object.get("cleanScore").getAsInt());

        //google storage authorize file injection
        StorageOptions storageOptions = StorageOptions.newBuilder()
                .setProjectId("YOUR_PROJECT_ID")
                .setCredentials(GoogleCredentials.fromStream(new
                        FileInputStream("/ENGN/capstone-274707-8dc2e791e977.json"))).build();
//        FileInputStream("src\\main\\resources\\capstone-274707-8dc2e791e977.json"))).build();
        Storage storage = storageOptions.getService();

        dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dt = LocalDateTime.now();
        dtString = dt.format(dtf);

        reviewDto.setTattooUrl1(null);
        reviewDto.setTattooUrl2(null);
        //TattooUrl
        if (tattoos == null || tattoos.isEmpty()) {
            System.out.println("review don't have tattoo Image");

        } else if (tattoos.size() == 1 || tattoos.size() == 2) {

            for (int idx = 0; idx < tattoos.size(); idx++) {
                MultipartFile tattoo = tattoos.get(idx);

                String fileName = dtString + tattoo.getOriginalFilename();
                String cloudFileName = "review/tattoo/" + fileName;
                if (idx == 0)
                    reviewDto.setTattooUrl1("https://storage.googleapis.com/capstone-image-bucket/" + cloudFileName);
                else
                    reviewDto.setTattooUrl2("https://storage.googleapis.com/capstone-image-bucket/" + cloudFileName);

                BlobInfo blobInfo = storage.create(
                        BlobInfo.newBuilder("capstone-image-bucket", cloudFileName)
                                .build(),
                        tattoo.getBytes());
            }

        } else {
            System.out.println("review tattoo image size error");
        }

        //CleanUrl
        if (clean == null || clean.isEmpty()) {
            System.out.println("review don't have clean Image");
            reviewDto.setCleanUrl(null);
        } else {
            String fileName = dtString + clean.getOriginalFilename();
            String cloudFileName = "review/clean/" + fileName;

            reviewDto.setCleanUrl("https://storage.googleapis.com/capstone-image-bucket/" + cloudFileName);

            BlobInfo blobInfo = storage.create(
                    BlobInfo.newBuilder("capstone-image-bucket", cloudFileName)
                            .build(),
                    clean.getBytes());
        }
        System.out.println("write Review : " + reviewDto.toString());
        reviewService.writeReview(reviewDto);
    }

    @DeleteMapping("/delete/review")
    public void removeReview(@RequestParam(value = "reviewId") int reviewId) {
        System.out.println("delete reviewId : " + reviewId);
        reviewService.deleteReview(reviewId);
    }
}
