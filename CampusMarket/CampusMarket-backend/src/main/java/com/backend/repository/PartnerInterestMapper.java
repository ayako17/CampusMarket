package com.backend.repository;

import com.backend.entity.PartnerInterest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PartnerInterestMapper {

    void insertPartnerInterest(PartnerInterest interest);

    int deletePartnerInterest(@Param("id") Long id);

    Optional<PartnerInterest> findByUserAndPartner(@Param("userId") Long userId, @Param("partnerId") Long partnerId);

    List<PartnerInterest> findByUserId(@Param("userId") Long userId, @Param("pageable") Pageable pageable);
    int deleteByPartnerId(@Param("partnerId") Long partnerId);
    long countByUserId(@Param("userId") Long userId);
}