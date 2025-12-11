package com.backend.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Data
public class PartnerMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    @JsonBackReference
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matcher_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User matcher;

    private LocalDateTime matchedAt;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.PENDING;

    public enum MatchStatus {
        PENDING, ACCEPTED, REJECTED
    }
}
