package com.backend.repository;

import com.backend.entity.PartnerMatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable; // 用于 MyBatis 分页
import java.util.List;
import java.util.Optional;

@Mapper
public interface PartnerMatchMapper {

    void insertPartnerMatch(PartnerMatch match);

    int updatePartnerMatch(PartnerMatch match);

    Optional<PartnerMatch> findById(@Param("id") Long id);

    Optional<PartnerMatch> findByPartnerIdAndMatcherId(@Param("partnerId") Long partnerId, @Param("matcherId") Long matcherId);

    List<PartnerMatch> findByMatcherId(@Param("userId") Long userId, @Param("pageable") Pageable pageable);
    long countByMatcherId(@Param("userId") Long userId);

    List<PartnerMatch> findByPartnerUserId(@Param("userId") Long userId, @Param("pageable") Pageable pageable);
    long countByPartnerUserId(@Param("userId") Long userId);

    List<PartnerMatch> findByPartnerId(@Param("partnerId") Long partnerId, @Param("pageable") Pageable pageable);

    long countAcceptedMatchesByPartnerId(@Param("partnerId") Long partnerId);

    int deleteByPartnerId(@Param("partnerId") Long partnerId);

    int deleteByMatcherId(@Param("matcherId") Long matcherId);
}