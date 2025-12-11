package com.backend.repository;

import com.backend.entity.Profile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface ProfileMapper {

    Optional<Profile> findByUserId(@Param("userId") Long userId);

    Optional<Profile> findById(@Param("id") Long id);

    void insertProfile(Profile profile);

    int updateProfile(Profile profile);
}