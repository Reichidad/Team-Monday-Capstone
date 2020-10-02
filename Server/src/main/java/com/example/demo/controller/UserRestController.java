package com.example.demo.controller;

import com.example.demo.datatype.PostDetail;
import com.example.demo.dto.UserDto;
import com.example.demo.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
public class UserRestController {
    private UserService userService;

    @GetMapping("/user")
    public UserDto getUser(@RequestParam(value = "userId")String userId){
        UserDto userDto = userService.getUser(userId);
        if(userDto != null)
            System.out.println("UserController.getUser : " + userDto.toString());
        else {
            System.out.println("id : " + userId + " - " + "don't have user");
            return null;
        }

        return userDto;
    }

    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void signUp(@RequestBody UserDto userDto){
        System.out.println("insert : " + userDto.toString());
        userService.addUser(userDto);
    }

    @DeleteMapping("/delete/user")
    public void deleteAccount(@RequestParam(value = "userId")String userId){
        System.out.println("delete userId : " + userId);
        userService.deleteOneUser(userId);
    }
}
