package com.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

@Data
@Entity
@Table(name = "profiles")
public class Profile implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String name;
    private String grade;
    private String college;//学院
    private String contact;
    private String address;
    private String avatarUrl;//头像
    private double latitude;//纬度
    private double longitude;//经度

    @Column(length = 500)
    private String bio;
}