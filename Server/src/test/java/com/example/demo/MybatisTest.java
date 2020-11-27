package com.example.demo;

import com.example.demo.dao.UserMapper;
import com.example.demo.dto.UserDto;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@org.mybatis.spring.boot.test.autoconfigure.MybatisTest
public class MybatisTest {

    @Test
    public void test() {
    }
}
