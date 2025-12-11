package com.backend.dto;
import com.backend.entity.Partner;
public class PartnerSearchDTO {
    private String subject;
    private Partner.PartnerStatus status;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Partner.PartnerStatus getStatus() {
        return status;
    }

    public void setStatus(Partner.PartnerStatus status) {
        this.status = status;
    }
}