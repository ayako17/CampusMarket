package com.backend.service;

import com.backend.entity.Profile;
import com.backend.repository.ProfileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 确保导入事务注解
import java.util.Optional;

@Service
public class ProfileService {
    @Autowired
    private ProfileMapper profileMapper;
    // 根据用户ID获取个人资料
    public Optional<Profile> getProfileByUserId(Long userId) {
        return profileMapper.findByUserId(userId);
    }
    // 创建或更新个人资料
    @Transactional
    public Profile createOrUpdateProfile(Profile profile) {
        if (profile.getId() != null) {
            profileMapper.findById(profile.getId())
                    .orElseThrow(() -> new RuntimeException("Profile not found for update with ID: " + profile.getId()));
            profileMapper.updateProfile(profile);
            return profile;
        } else {
            profileMapper.insertProfile(profile);
            return profile;
        }
    }
}