package com.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import com.backend.entity.PartnerMatch;

@Data
public class PartnerMatchDTO {
    private Long id;
    private PartnerSimpleDTO partner;
    private UserSimpleDTO matcher;
    private LocalDateTime matchedAt;
    private PartnerMatch.MatchStatus status;

    @Data
    public static class PartnerSimpleDTO {
        private Long id;
        private String title;
        private String subject;
        private UserSimpleDTO user;
    }

    @Data
    public static class UserSimpleDTO {
        private Long id;
        private String username;
    }
}