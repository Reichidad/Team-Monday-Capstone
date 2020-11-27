package com.example.demo.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.example.demo.dto.UserDto;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface UserMapper {

    public void insertUser(UserDto userDto);
    public UserDto selectOneUser (String userId);
    public void deleteUser (String userId);

//    public List<UserDto> selectAllUser();
//    public void insertUser (UserDto user);
//    public void updateUser (UserDto user);
}