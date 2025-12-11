package com.backend.service;

import com.backend.entity.User;
import com.backend.repository.UserMapper;
import com.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserMapper userMapper;
    public Optional<User> login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public String generateToken(Long userId) {
        return JwtUtil.generateToken(userId);
    }
    @Transactional // 保持事务管理
    public User register(User user) {
        if (userMapper.existsByUsername(user.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        userMapper.insertUser(user);
        return user; // 返回包含 ID 的 User 对象
    }
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    public User findById(Long userId) {
        return userMapper.findById(userId);
    }
}