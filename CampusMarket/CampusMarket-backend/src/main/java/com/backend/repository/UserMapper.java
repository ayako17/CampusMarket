package com.backend.repository;

import com.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    User findByUsername(@Param("username") String username);

    User findById(@Param("id") Long id);

    int existsByUsername(@Param("username") String username);

    void insertUser(User user);

    User findByIdSimple(@Param("id") Long id);
}