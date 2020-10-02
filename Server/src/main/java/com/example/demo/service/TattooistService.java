package com.example.demo.service;

import com.example.demo.dao.TattooistMapper;
import com.example.demo.dto.TattooistDto;
import com.example.demo.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class TattooistService {
    private TattooistMapper tattooistMapper;

    public void addTattooist(TattooistDto tattooistDto){
        tattooistMapper.insertTattooist(tattooistDto);
    }

    public TattooistDto getTattooist (String userId){
        TattooistDto tattooistDto = tattooistMapper.selectOneTattooist(userId);
        return tattooistDto;
    }
}
