package com.example.demo.controller;

import com.example.demo.datatype.PostDetail;
import com.example.demo.service.PostService;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
    test : http://localhost:8080/swagger-ui.html
 */
@org.springframework.web.bind.annotation.RestController("PostRestController")
@AllArgsConstructor
public class PostRestController {
    private PostService postService;

    @GetMapping("/allpost")
    public ArrayList<PostDetail> getAllPost() {
        ArrayList<PostDetail> posts = postService.getAllPost();
//        System.out.println("post size : " + posts.size());
//        for (PostDetail post : posts) {
//            System.out.println(post.toString());
//        }
        System.out.println("get all post request");
        return posts;
    }

    //    public void writePost(@RequestPart PostDetail post, @RequestPart("file")MultipartFile file){
//    public void writePost(@RequestPart PostDetail post,
//                          @RequestParam Map<String, MultipartFile> fileMap){
    @PostMapping(value = "/write/post", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public void writePost(@RequestPart String post,
                          @RequestParam(value = "tattoos") List<MultipartFile> tattoos,
//                          @RequestParam List<MultipartFile> tattoos,
                          @RequestParam(value = "design", required = false) MultipartFile design) throws ParseException, IOException {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(post);
        JsonObject object = element.getAsJsonObject();
        PostDetail postDetail = new PostDetail();

        postDetail.setTattooistId(object.get("tattooistId").getAsString());
        postDetail.setTitle(object.get("title").getAsString());
        postDetail.setDescription(object.get("description").getAsString());
        postDetail.setPrice(object.get("price").getAsInt());
        postDetail.setLikeNum(0); // like = 0 at start time
        postDetail.setGenre(object.get("genre").getAsString());
        postDetail.setBigShape(object.get("bigShape").getAsString());
        postDetail.setSmallShape(object.get("smallShape").getAsString());
        postDetail.setAvgCleanScore(0);

        //google storage authorize file injection
        StorageOptions storageOptions = StorageOptions.newBuilder()
                .setProjectId("YOUR_PROJECT_ID")
                .setCredentials(GoogleCredentials.fromStream(new
                        FileInputStream("/ENGN/capstone-274707-8dc2e791e977.json"))).build();
//                        FileInputStream("src\\main\\resources\\capstone-274707-8dc2e791e977.json"))).build();
        Storage storage = storageOptions.getService();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dt = LocalDateTime.now();
        String dtString = dt.format(dtf);

        if (design == null || design.isEmpty()) {
            System.out.println("don't have design");
            postDetail.setDesignUrl(null);
        } else {
//            String fileName = dtString + design.getOriginalFilename();
            String fileName = design.getOriginalFilename();
            String cloudFileName = "design/" + fileName;

//            System.out.println("https://storage.googleapis.com/capstone-image-bucket/" + cloudFileName);
            postDetail.setDesignUrl("https://storage.googleapis.com/capstone-image-bucket/" + cloudFileName);

            BlobInfo blobInfo = storage.create(
                    BlobInfo.newBuilder("capstone-image-bucket", cloudFileName)
                            .build(),
                        transformDesign(design));
//                    design.getBytes());
        }

        postDetail.setTattooUrl(new ArrayList<String>());
        for (MultipartFile tattoo : tattoos) {
//            String fileName = dtString + tattoo.getOriginalFilename();
            String fileName = tattoo.getOriginalFilename();
            String cloudFileName = "tattoo/" + fileName;
//            System.out.println("https://storage.googleapis.com/capstone-image-bucket/" + cloudFileName);
            postDetail.getTattooUrl().add("https://storage.googleapis.com/capstone-image-bucket/" + cloudFileName);

            // don't have to set acl because of bucket authorization policy
//            .setAcl(new ArrayList<>(Arrays.asList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
            BlobInfo blobInfo = storage.create(
                    BlobInfo.newBuilder("capstone-image-bucket", cloudFileName)
                            .build(),
                    tattoo.getBytes());
        }

//        System.out.println("write : " + postDetail.toString());
        postService.writePost(postDetail);
    }

    @GetMapping("/tattooistposts")
    public ArrayList<PostDetail> getTattooistPost(@RequestParam(value = "tattooistId") String tattooistId){
        ArrayList<PostDetail> posts = postService.getPostByTattooist(tattooistId);

        return posts;
    }


    @DeleteMapping("/delete/post")
    public void removePost(@RequestParam(value = "postId") int postId) {
        System.out.println("delete postId : " + postId);
        postService.deleteAPost(postId);
    }

    private byte[] transformDesign(MultipartFile design) throws IOException {

        BufferedImage source = ImageIO.read(design.getInputStream());

        final int color = source.getRGB(0, 0);

        Image imageWithTransparency = makeColorTransparent(source, new Color(color), 10);

        BufferedImage transparentImage = imageToBufferedImage(imageWithTransparency);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(transparentImage, "PNG", baos);
        baos.flush();
        byte[] output = baos.toByteArray();
        return output;
    }

    /**
     * Convert Image to BufferedImage.
     *
     * @param image Image to be converted to BufferedImage.
     * @return BufferedImage corresponding to provided Image.
     */
    private static BufferedImage imageToBufferedImage(final Image image) {
        final BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }

    /**
     * Make provided image transparent wherever color matches the provided color.
     *
     * @param im    BufferedImage whose color will be made transparent.
     * @param color Color in provided image which will be made transparent.
     * @return Image with transparency applied.
     */
    private static Image makeColorTransparent(final BufferedImage im, final Color color, int tolerance) {
        int temp = 0;
        if (tolerance < 0 || tolerance > 100) {
            System.err.println("The tolerance is a percentage, so the value has to be between 0 and 100.");
            temp = 0;
        } else {
            temp = tolerance * (0xFF000000 | 0xFF000000) / 100;
        }

        final int toleranceRGB = Math.abs(temp);
        final ImageFilter filter = new RGBImageFilter() {

            // The color we are looking for (white)... Alpha bits are set to opaque
            public int markerRGBFrom = (color.getRGB() | 0xFF000000) - toleranceRGB;
            public int markerRGBTo = (color.getRGB() | 0xFF000000) + toleranceRGB;

            public final int filterRGB(final int x, final int y, final int rgb) {
                if ((rgb | 0xFF000000) >= markerRGBFrom && (rgb | 0xFF000000) <= markerRGBTo) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // Nothing to do
                    return rgb;
                }
            }
        };

        final ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

}
