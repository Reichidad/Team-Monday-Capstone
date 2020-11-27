package com.example.demo.dao;

import com.example.demo.dto.TattooImageDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Map;

@Repository
@Mapper
public interface TattooImageMapper {
    //TODO
    public void insertTattooImage(TattooImageDto tattooImageDto);
    public ArrayList<TattooImageDto> searchPostIdList(Map<String, Object> map);

    public String selectTattooDesign(int postId);
}
