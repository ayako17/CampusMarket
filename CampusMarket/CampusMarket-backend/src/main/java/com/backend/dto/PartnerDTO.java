package com.backend.dto;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.backend.entity.Partner;
@Data
public class PartnerDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UserDTO user;

    private String title;
    private String description;
    private String subject;
    private String preferredTime;
    private String contactMethod;

    private Integer matchedCount; // 已匹配人数
    private Integer targetCount; // 目标匹配人数，默认为1

    private Partner.PartnerStatus status;

    private LocalDateTime createdAt;
}
