package com.backend.repository;

import com.backend.entity.Partner;
import com.backend.entity.Partner.PartnerStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PartnerMapper {

    void insertPartner(Partner partner);

    int updatePartner(Partner partner);

    Optional<Partner> findById(@Param("id") Long id);

    List<Partner> findByUserIdAndStatus(@Param("userId") Long userId,
                                        @Param("status") PartnerStatus status,
                                        @Param("pageable") Pageable pageable);

    List<Partner> findBySubjectContainingAndStatus(@Param("subject") String subject,
                                                   @Param("status") PartnerStatus status,
                                                   @Param("pageable") Pageable pageable);

    List<Partner> findBySubjectContaining(@Param("subject") String subject,
                                          @Param("pageable") Pageable pageable);

    List<Partner> findByStatus(@Param("status") PartnerStatus status,
                               @Param("pageable") Pageable pageable);

    List<Partner> findByStatusOrderByCreatedAtDesc(@Param("status") PartnerStatus status,
                                                   @Param("pageable") Pageable pageable);

    int updateMatchedCount(@Param("id") Long id, @Param("count") Integer count);

    int updateTargetCount(@Param("id") Long id, @Param("count") Integer count);

    List<Partner> findAvailablePartners(@Param("pageable") Pageable pageable);
    int deletePartnerById(@Param("id") Long id);
    long countPartnersBySearchCriteria(@Param("subject") String subject,
                                       @Param("status") PartnerStatus status);
    long countByUserIdAndStatus(@Param("userId") Long userId,
                                @Param("status") PartnerStatus status);

    long countAllPartners();
    Optional<Partner> findByIdSimple(@Param("id") Long id);
}