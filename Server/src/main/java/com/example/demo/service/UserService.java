package com.example.demo.service;

import com.example.demo.dao.UserMapper;
import com.example.demo.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserService {
    private UserMapper userMapper;

    public void addUser(UserDto userDto){
        userMapper.insertUser(userDto);
    }

    public UserDto getUser (String userId){
        UserDto user = userMapper.selectOneUser(userId);
        return user;
    }

    public void deleteOneUser(String userId){
        userMapper.deleteUser(userId);
    }
}
