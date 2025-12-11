package com.backend.controller;

import com.backend.entity.PartnerMatch;
import com.backend.dto.PartnerMatchDTO;
import com.backend.service.PartnerService;
import com.backend.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Slf4j
public class MatchController {
    private final JwtUtil jwtUtil;
    private final PartnerService partnerService;

    // DTO 转换辅助方法
    private PartnerMatchDTO convertToDTO(PartnerMatch match) {
        PartnerMatchDTO dto = new PartnerMatchDTO();
        dto.setId(match.getId());
        dto.setMatchedAt(match.getMatchedAt());
        dto.setStatus(match.getStatus());
        // 简化 Partner DTO
        PartnerMatchDTO.PartnerSimpleDTO partnerSimpleDTO = new PartnerMatchDTO.PartnerSimpleDTO();
        partnerSimpleDTO.setId(match.getPartner().getId());
        partnerSimpleDTO.setTitle(match.getPartner().getTitle());
        partnerSimpleDTO.setSubject(match.getPartner().getSubject());
        // 简化 User DTO (帖子发布者)
        PartnerMatchDTO.UserSimpleDTO partnerUserDTO = new PartnerMatchDTO.UserSimpleDTO();
        partnerUserDTO.setId(match.getPartner().getUser().getId());
        partnerUserDTO.setUsername(match.getPartner().getUser().getUsername());
        partnerSimpleDTO.setUser(partnerUserDTO);
        dto.setPartner(partnerSimpleDTO);
        // 简化 Matcher DTO (申请人)
        PartnerMatchDTO.UserSimpleDTO matcherDTO = new PartnerMatchDTO.UserSimpleDTO();
        matcherDTO.setId(match.getMatcher().getId());
        matcherDTO.setUsername(match.getMatcher().getUsername());
        dto.setMatcher(matcherDTO);
        return dto;
    }

    // 匹配请求 - 检查是否已匹配
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkIfMatched(
            @RequestParam Long partnerId,
            @RequestHeader("Authorization") String token) {

        try {
            // 1. 验证 token 并获取 userId
            Long userId = jwtUtil.validateToken(token.replace("Bearer ", ""));
            // 2. 调用 Service 层方法实现逻辑
            boolean isMatched = partnerService.checkIfMatched(partnerId, userId);
            // 3. 返回结果
            return ResponseEntity.ok(isMatched);
        } catch (RuntimeException e) {
            // 捕获 token 验证失败的情况，返回 401 Unauthorized
            log.error("Token验证失败或匹配检查失败: {}", e.getMessage());
            // 身份验证失败，返回 401 状态，Body 为 false
            return ResponseEntity.status(401).body(false);
        }
    }
    // 匹配记录查询
    @GetMapping("/requests")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getMyMatches(
            // 通过 try-catch 增强错误信息
            @RequestHeader("Authorization") String token,
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 1. 认证和获取用户ID
            // 如果 token 格式错误或验证失败，validateToken 应该抛出异常
            Long userId = jwtUtil.validateToken(token.replace("Bearer ", ""));
            Pageable pageable = PageRequest.of(page, size, Sort.by("matchedAt").descending());
            Page<PartnerMatch> matchesPage;
            // 2. 参数校验和业务逻辑调用
            if ("sent".equalsIgnoreCase(type)) {
                // 我发出的请求
                matchesPage = partnerService.getMatchesForUser(userId, pageable);
            } else if ("received".equalsIgnoreCase(type)) {
                // 我收到的请求
                List<PartnerMatch> receivedMatches = partnerService.getReceivedMatchesForUser(userId, pageable);
                long totalCount = partnerService.countReceivedMatchesForUser(userId);
                matchesPage = new PageImpl<>(receivedMatches, pageable, totalCount);
            } else {
                // 3. 无效参数处理 -> 返回 400 Bad Request
                return ResponseEntity.status(400).body(Map.of(
                        "error", "无效参数",
                        "message", "查询类型 'type' 必须是 'sent' 或 'received'"));
            }
            // 4. 转换为 DTO 并返回
            Page<PartnerMatchDTO> dtoPage = matchesPage.map(this::convertToDTO);
            return ResponseEntity.ok(dtoPage);
        } catch (EntityNotFoundException e) {
            // 用户ID未找到或Partner未找到 (404)
            return ResponseEntity.status(404).body(Map.of("error", "资源未找到", "message", e.getMessage()));
        } catch (Exception e) {
            // 捕获 JWT 验证失败、Token 格式错误等异常 (400)
            log.error("获取匹配请求列表失败", e);
            return ResponseEntity.status(400).body(Map.of("error", "认证或请求失败", "message", e.getMessage()));
        }
    }
    // 匹配请求 - 发起
    @PostMapping("/{partnerId}/apply")
    public ResponseEntity<?> createMatchRequest(
            @PathVariable Long partnerId,
            @RequestHeader("Authorization") String token) {
        try {
            Long matcherId = jwtUtil.validateToken(token.replace("Bearer ", ""));
            // 调用 Service 层方法
            PartnerMatch match = partnerService.matchPartner(partnerId, matcherId);
            // 响应体中返回 DTO
            return ResponseEntity.ok(Map.of(
                    "message", "匹配请求已发送",
                    "match", convertToDTO(match)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "资源不存在",
                    "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "操作无效",
                    "message", e.getMessage()));
        } catch (Exception e) {
            log.error("创建匹配请求失败", e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "服务器内部错误",
                    "message", "处理匹配请求时发生错误"));
        }
    }
    // 匹配请求 - 处理 (接受/拒绝)
    @PostMapping("/{partnerId}/requests/{matchId}")
    public ResponseEntity<?> processMatchRequest(
            @PathVariable Long partnerId,
            @PathVariable Long matchId,
            @RequestParam boolean accept,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.validateToken(token.replace("Bearer ", ""));
            Map<String, Object> result = partnerService.processMatchRequest(partnerId, matchId, userId, accept);
            PartnerMatch updatedMatch = (PartnerMatch) result.get("updatedMatch");
            return ResponseEntity.ok(Map.of(
                    "message", accept ? "匹配请求已接受" : "匹配请求已拒绝",
                    "updatedMatch", convertToDTO(updatedMatch),
                    "updatedPartner", result.get("updatedPartner")));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "处理请求失败",
                    "message", e.getMessage()));
        }
    }
}