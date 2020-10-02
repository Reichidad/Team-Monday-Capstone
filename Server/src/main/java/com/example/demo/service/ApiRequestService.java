package com.example.demo.service;


import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@AllArgsConstructor
@Service
public class ApiRequestService {

    RestTemplate restTemplate;

    public String request(final MultipartFile image) throws IOException {
        ResponseEntity<String> response = restTemplate.postForEntity("http://34.64.157.193:5000/test", getRequest(image), String.class);
//        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:5000/test", getRequest(image), String.class);

        return response.getBody();
    }

    private HttpEntity<MultiValueMap<String, Object>> getRequest(final MultipartFile image) throws IOException {

        ByteArrayResource fileResource = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("apiRequest", apiRequest);
        body.add("file", fileResource);

        return new HttpEntity<>(body, headers);
    }


}
