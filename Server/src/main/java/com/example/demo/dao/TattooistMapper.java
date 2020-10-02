package com.example.demo.dao;

import com.example.demo.dto.TattooistDto;
import com.example.demo.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface TattooistMapper {
    public void insertTattooist(TattooistDto tattooistDto);
    public TattooistDto selectOneTattooist (String userId);
}
