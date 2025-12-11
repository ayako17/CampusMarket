package com.backend.controller;

import com.backend.dto.PartnerDTO;
import com.backend.dto.PartnerSearchDTO;
import com.backend.dto.UserDTO;
import com.backend.entity.Partner;
import com.backend.repository.PartnerMapper;
import com.backend.repository.PartnerMatchMapper;
import com.backend.service.PartnerService;
import com.backend.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional; // 确保导入 Optional

@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
public class PartnerController {
    private final PartnerService partnerService;
    private final JwtUtil jwtUtil;
    private final PartnerMatchMapper partnerMatchRepository;
    private final PartnerMapper partnerRepository;
    private PartnerDTO convertToDTO(Partner partner) {
        PartnerDTO dto = new PartnerDTO();
        dto.setId(partner.getId());
        dto.setTitle(partner.getTitle());
        dto.setDescription(partner.getDescription());
        dto.setSubject(partner.getSubject());
        dto.setPreferredTime(partner.getPreferredTime());
        dto.setContactMethod(partner.getContactMethod());
        dto.setMatchedCount(partner.getMatchedCount());
        dto.setTargetCount(partner.getTargetCount());
        dto.setStatus(partner.getStatus());
        dto.setCreatedAt(partner.getCreatedAt());
        if (partner.getUser() != null) {
            dto.setUser(new UserDTO(partner.getUser().getId(), partner.getUser().getUsername()));
        }
        return dto;
    }
    // 创建伙伴请求
    @PostMapping(value = "/create", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> createPartner(
            @RequestBody Partner partner,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.validateToken(token.replace("Bearer ", ""));
            Partner createdPartner = partnerService.createPartner(partner, userId);
            return ResponseEntity.ok(convertToDTO(createdPartner));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", "用户未找到", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "服务器内部错误", "message", e.getMessage()));
        }
    }
    // 获取单个伙伴请求详情
    @GetMapping("/{id}")
    public ResponseEntity<PartnerDTO> getPartnerById(@PathVariable Long id) {
        Optional<Partner> partnerOptional = partnerService.getPartnerById(id);
        if (partnerOptional.isPresent()) {
            return ResponseEntity.ok(convertToDTO(partnerOptional.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    // 更新伙伴请求
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePartner(
            @PathVariable Long id,
            @RequestBody Partner partner,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.validateToken(token.replace("Bearer ", ""));
            Partner existingPartner = partnerService.getPartnerById(id)
                    .orElseThrow(() -> new EntityNotFoundException("伙伴请求不存在"));
            if (!existingPartner.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "error", "权限不足",
                        "message", "您无权修改此伙伴请求"));
            }
            partner.setId(id);
            partner.setUser(existingPartner.getUser());
            Partner updatedPartner = partnerService.updatePartner(partner);
            return ResponseEntity.ok(convertToDTO(updatedPartner));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", "资源不存在", "message", e.getMessage()));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "更新伙伴请求失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // 删除伙伴请求
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePartner(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.validateToken(token.replace("Bearer ", ""));
            partnerService.deletePartner(id, userId);
            return ResponseEntity.ok(Map.of("message", "伙伴请求删除成功"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", "资源不存在", "message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "权限不足", "message", e.getMessage()));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "删除伙伴请求失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // 搜索伙伴请求 (分页和过滤)
    @GetMapping("/search")
    public ResponseEntity<Page<PartnerDTO>> searchPartners(
            PartnerSearchDTO searchDTO,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<Partner> partners = partnerService.searchPartners(searchDTO, pageable);
            // 转换为 DTO
            Page<PartnerDTO> dtoPage = partners.map(this::convertToDTO);
            return ResponseEntity.ok(dtoPage);
        } catch (Exception e) {
            // 更好的错误处理，但返回类型是 Page<PartnerDTO>，所以需要返回一个 Page
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }
    // 收藏/取消收藏
    @PostMapping("/{id}/partnerInterest")
    public ResponseEntity<?> togglePartnerInterest(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.validateToken(token.replace("Bearer ", ""));
            partnerService.togglePartnerInterest(id, userId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", "资源不存在", "message", e.getMessage()));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "收藏操作失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // 获取收藏列表
    @GetMapping("/partnerInterests")
    public ResponseEntity<Page<PartnerDTO>> getPartnerInterests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.validateToken(token.replace("Bearer ", ""));
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Partner> partners = partnerService.getPartnerInterests(userId, pageable);
            // 转换为DTO
            Page<PartnerDTO> dtoPage = partners.map(this::convertToDTO);
            return ResponseEntity.ok(dtoPage);
        } catch (EntityNotFoundException e) {
            // 如果用户不存在，返回空页
            return ResponseEntity.status(404).body(Page.empty());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }
    @GetMapping
    public ResponseEntity<Page<PartnerDTO>> searchPartners(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String statusFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PartnerSearchDTO searchDTO = new PartnerSearchDTO();
        // 1. 处理 subject：如果为空字符串或 null，则不设置
        if (subject != null && !subject.trim().isEmpty()) {
            searchDTO.setSubject(subject.trim());
        }
        // 2. 处理 statusFilter：如果传入了有效的状态，则转换
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            try {
                // 尝试将字符串转换为枚举
                searchDTO.setStatus(Partner.PartnerStatus.valueOf(statusFilter.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // 如果传入的状态字符串无效，返回 400 错误
                Map<String, String> error = new HashMap<>();
                error.put("message", "状态参数无效: " + statusFilter);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Page.empty());
            }
        }
        // 默认排序：按创建时间降序
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        try {
            Page<Partner> partners = partnerService.searchPartners(searchDTO, pageable);
            // 转换为DTO
            Page<PartnerDTO> dtoPage = partners.map(this::convertToDTO);
            return ResponseEntity.ok(dtoPage);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取伙伴列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "服务器内部错误: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}