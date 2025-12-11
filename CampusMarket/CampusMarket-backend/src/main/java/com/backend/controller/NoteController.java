package com.backend.controller;

import com.backend.entity.Note;
import com.backend.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "http://localhost:5173")
public class NoteController {
    @Autowired
    private NoteService noteService;
    // 获取所有资料信息
    @GetMapping
    public ResponseEntity<?> getAllNotes() {
        try {
            List<Note> notes = noteService.getAllNotes();
            Map<String, Object> response = new HashMap<>();
            response.put("notes", notes);
            response.put("message", "Notes retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve notes: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    // 添加资料
    @PostMapping
    public ResponseEntity<?> addNote(@RequestBody Note note) {
        try {
            Note savedNote = noteService.addNote(note);
            Map<String, Object> response = new HashMap<>();
            response.put("note", savedNote);
            response.put("message", "Note added successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to add note: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    // 删除资料
    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote(@PathVariable Long noteId) {
        try {
            noteService.deleteNote(noteId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Note deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete note: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    // 修改资料
    @PutMapping("/{noteId}")
    public ResponseEntity<?> updateNote(@RequestBody Note note) {
        try {
            Note updatedNote = noteService.updateNote(note);
            Map<String, Object> response = new HashMap<>();
            response.put("note", updatedNote);
            response.put("message", "Note updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to update note: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    // 根据关键字搜索资料
    @GetMapping("/search")
    public ResponseEntity<?> searchNotes(@RequestParam String keyword) {
        try {
            List<Note> notes = noteService.searchNotes(keyword);
            Map<String, Object> response = new HashMap<>();
            response.put("notes", notes);
            response.put("message", "Search completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to search notes: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    // 根据用户ID获取资料
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getNotesByUserId(@PathVariable Long userId) {
        try {
            List<Note> notes = noteService.getNotesByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("notes", notes);
            response.put("message", "User notes retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve user notes: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}