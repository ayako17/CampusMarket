package com.backend.controller;

import com.backend.entity.Profile;
import com.backend.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfileController {
    @Autowired
    private ProfileService profileService;
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        Optional<Profile> profileOptional = profileService.getProfileByUserId(userId);
        if (profileOptional.isPresent()) {
            return ResponseEntity.ok(profileOptional.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Profile not found");
            return ResponseEntity.badRequest().body(error);
        }
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody Profile profile) {
        Profile updatedProfile = profileService.createOrUpdateProfile(profile);
        return ResponseEntity.ok(updatedProfile);
    }
}