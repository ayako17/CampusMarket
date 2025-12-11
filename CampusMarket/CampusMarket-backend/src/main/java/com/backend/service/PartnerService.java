package com.backend.service;

import com.backend.dto.PartnerSearchDTO;
import com.backend.entity.Partner;
import com.backend.entity.PartnerMatch;
import com.backend.entity.User;
import com.backend.entity.PartnerInterest;
import com.backend.repository.PartnerMatchMapper;
import com.backend.repository.PartnerMapper;
import com.backend.repository.UserMapper;
import com.backend.repository.PartnerInterestMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class PartnerService {
    private final Logger log = LoggerFactory.getLogger(PartnerService.class);
    private final PartnerMapper partnerMapper;
    private final PartnerMatchMapper matchMapper;
    private final UserMapper userMapper;
    private final PartnerInterestMapper partnerInterestMapper;
    // 创建伙伴请求
    @Transactional
    public Partner createPartner(Partner partner, Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new EntityNotFoundException("用户不存在");
        }

        if (partner.getTargetCount() == null || partner.getTargetCount() < 1) {
            partner.setTargetCount(1);
        }
        partner.setMatchedCount(0);
        partner.setUser(user);

        partnerMapper.insertPartner(partner);
        return partner;
    }

    // 获取特定伙伴请求
    public Optional<Partner> getPartnerById(Long id) {
        return partnerMapper.findById(id);
    }

    // 更新伙伴请求
    @Transactional
    public Partner updatePartner(Partner partner) {
        partnerMapper.updatePartner(partner);
        return partner;
    }

    // 删除伙伴请求
    @Transactional
    public void deletePartner(Long partnerId, Long userId) {
        Partner partner = partnerMapper.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("伙伴请求不存在"));
        // 验证用户是否有权删除：只有创建者可以删除
        if (!partner.getUser().getId().equals(userId)) {
            throw new SecurityException("无权删除此伙伴请求，您不是创建者");
        }
        // 1. 删除所有相关的匹配记录
        // 假设 PartnerMatchMapper 有 deleteByPartnerId 方法
        matchMapper.deleteByPartnerId(partnerId);
        // 2. 删除所有相关的收藏记录
        // 假设 PartnerInterestMapper 有 deleteByPartnerId 方法
        partnerInterestMapper.deleteByPartnerId(partnerId);
        // 3. 删除伙伴请求
        partnerMapper.deletePartnerById(partnerId);
    }

    // 匹配伙伴 (发起匹配请求)
    @Transactional
    public PartnerMatch matchPartner(Long partnerId, Long matcherId) {
        Partner partner = partnerMapper.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("伙伴请求不存在"));
        User matcher = userMapper.findById(matcherId);
        if (matcher == null) {
            throw new EntityNotFoundException("匹配用户不存在");
        }
        if (partner.getStatus() != Partner.PartnerStatus.ACTIVE) {
            throw new IllegalArgumentException("该伙伴请求已非活跃状态，无法匹配");
        }
        if (partner.getMatchedCount() >= partner.getTargetCount()) {
            throw new IllegalArgumentException("该伙伴请求已达到目标匹配人数");
        }
        if (matchMapper.findByPartnerIdAndMatcherId(partnerId, matcherId).isPresent()) {
            throw new IllegalArgumentException("您已经匹配过该伙伴请求");
        }
        PartnerMatch match = new PartnerMatch();
        match.setPartner(partner);
        match.setMatcher(matcher);
        match.setStatus(PartnerMatch.MatchStatus.PENDING);
        match.setMatchedAt(LocalDateTime.now());
        matchMapper.insertPartnerMatch(match);
        return match;
    }

    // 处理匹配请求（接受/拒绝）
    @Transactional
    public Map<String, Object> processMatchRequest(Long partnerId, Long matchId, Long userId, boolean accept) {
        PartnerMatch match = matchMapper.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("匹配记录不存在"));

        if (!match.getPartner().getId().equals(partnerId)) {
            throw new IllegalArgumentException("匹配ID与伙伴请求ID不一致");
        }
        if (!match.getPartner().getUser().getId().equals(userId)) {
            throw new SecurityException("无权处理此匹配请求");
        }
        if (accept) {
            match.setStatus(PartnerMatch.MatchStatus.ACCEPTED);
        } else {
            match.setStatus(PartnerMatch.MatchStatus.REJECTED);
        }

        matchMapper.updatePartnerMatch(match);

        Partner partner = partnerMapper.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("伙伴请求不存在"));

        long acceptedCount = matchMapper.countAcceptedMatchesByPartnerId(partnerId);
        partner.setMatchedCount((int) acceptedCount);

        if (partner.getStatus() == Partner.PartnerStatus.ACTIVE && partner.getMatchedCount() >= partner.getTargetCount()) {
            partner.setStatus(Partner.PartnerStatus.MATCHED);
        } else if (partner.getStatus() == Partner.PartnerStatus.MATCHED && partner.getMatchedCount() < partner.getTargetCount()) {
            partner.setStatus(Partner.PartnerStatus.ACTIVE);
        }

        partnerMapper.updatePartner(partner);

        Map<String, Object> result = new HashMap<>();
        result.put("updatedMatch", match);
        result.put("updatedPartner", partner);
        return result;
    }

    // 获取指定用户的匹配列表 (作为申请者, 'sent' requests)
    public Page<PartnerMatch> getMatchesForUser(Long userId, Pageable pageable) {
        List<PartnerMatch> matches = matchMapper.findByMatcherId(userId, pageable);
        long total = matchMapper.countByMatcherId(userId);
        return new PageImpl<>(matches, pageable, total);
    }

    // 获取指定用户的匹配列表 (作为帖子发布者, 'received' requests)
    public List<PartnerMatch> getReceivedMatchesForUser(Long userId, Pageable pageable) {
        return matchMapper.findByPartnerUserId(userId, pageable);
    }

    // 获取指定用户的匹配列表总数 (作为帖子发布者)
    public long countReceivedMatchesForUser(Long userId) {
        return matchMapper.countByPartnerUserId(userId);
    }

    // 获取指定伙伴请求的匹配列表
    public List<PartnerMatch> getMatchesForPartner(Long partnerId) {
        return matchMapper.findByPartnerId(partnerId, Pageable.unpaged());
    }
    public boolean checkIfMatched(Long partnerId, Long userId) {
        return matchMapper.findByPartnerIdAndMatcherId(partnerId, userId).isPresent();
    }

    // 切换收藏状态
    @Transactional
    public void togglePartnerInterest(Long partnerId, Long userId) {
        partnerMapper.findById(partnerId)
                .orElseThrow(() -> new EntityNotFoundException("伙伴请求不存在"));

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new EntityNotFoundException("用户不存在");
        }

        Optional<PartnerInterest> existing = partnerInterestMapper.findByUserAndPartner(userId, partnerId);

        if (existing.isPresent()) {
            partnerInterestMapper.deletePartnerInterest(existing.get().getId());
        } else {
            PartnerInterest partnerInterest = new PartnerInterest();
            partnerInterest.setUser(user);
            partnerInterest.setPartner(partnerMapper.findById(partnerId).get());

            partnerInterestMapper.insertPartnerInterest(partnerInterest);
        }
    }

    // 获取用户收藏
    public Page<Partner> getPartnerInterests(Long userId, Pageable pageable) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new EntityNotFoundException("用户不存在");
        }

        List<PartnerInterest> interests = partnerInterestMapper.findByUserId(userId, pageable);

        List<Partner> partners = interests.stream()
                .map(PartnerInterest::getPartner)
                .toList();

        long total = partnerInterestMapper.countByUserId(userId);

        return new PageImpl<>(partners, pageable, total);
    }

    // 搜索伙伴请求
    public Page<Partner> searchPartners(PartnerSearchDTO searchDTO, Pageable pageable) {
        List<Partner> partners;
        long total;

        String subject = searchDTO.getSubject();
        Partner.PartnerStatus status = searchDTO.getStatus();

        if (subject != null && !subject.isEmpty() && status != null) {
            partners = partnerMapper.findBySubjectContainingAndStatus(subject, status, pageable);
            total = partnerMapper.countPartnersBySearchCriteria(subject, status);
        } else if (subject != null && !subject.isEmpty()) {
            partners = partnerMapper.findBySubjectContaining(subject, pageable);
            total = partnerMapper.countPartnersBySearchCriteria(subject, null);
        } else if (status != null) {
            partners = partnerMapper.findByStatus(status, pageable);
            total = partnerMapper.countPartnersBySearchCriteria(null, status);
        } else {
            partners = partnerMapper.findByStatus(Partner.PartnerStatus.ACTIVE, pageable);
            total = partnerMapper.countPartnersBySearchCriteria(null, Partner.PartnerStatus.ACTIVE);
        }

        return new PageImpl<>(partners, pageable, total);
    }
}