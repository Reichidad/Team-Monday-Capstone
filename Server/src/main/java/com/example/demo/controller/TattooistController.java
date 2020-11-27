package com.example.demo.controller;

import com.example.demo.dto.TattooistDto;
import com.example.demo.dto.UserDto;
import com.example.demo.service.TattooistService;
import com.example.demo.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
public class TattooistController {
    private TattooistService tattooistService;

    @GetMapping("/tattooist")
    public TattooistDto getTattooist(@RequestParam(value = "userId")String userId){
        TattooistDto tattooistDto = tattooistService.getTattooist(userId);
        if(tattooistDto != null) {
//            System.out.println("TattooistController.getTattooist : " + tattooistDto.toString());
        } else {
            System.out.println("id : " + userId + " - " + "don't have tattooist");
            return null;
        }

        return tattooistDto;
    }

    @PostMapping(value = "/signup/tattooist", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void signUp(@RequestBody TattooistDto tattooistDto) {
        System.out.println("insert : " + tattooistDto.toString());
        tattooistService.addTattooist(tattooistDto);
    }
}
