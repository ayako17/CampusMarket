package com.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "partner")
@Data
public class Partner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;
    private String description;
    private String subject;
    private String preferredTime;
    private String contactMethod;

    @Enumerated(EnumType.STRING)
    private PartnerStatus status = PartnerStatus.ACTIVE;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private Integer targetCount; // 目标匹配数
    private Integer matchedCount; // 已匹配数
    @OneToMany(mappedBy = "partner", fetch = FetchType.LAZY)
    private List<PartnerMatch> matches;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum PartnerStatus {
        ACTIVE, INACTIVE, MATCHED
    }
}