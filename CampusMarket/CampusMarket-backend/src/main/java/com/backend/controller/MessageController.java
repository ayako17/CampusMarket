package com.backend.controller;

import com.backend.entity.Message;
import com.backend.entity.User;
import com.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:5173")
public class MessageController {
    @Autowired
    private MessageService messageService;
    // 发送消息
    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody Message message) {
        if (message.getSender() == null || message.getReceiver() == null) {
            return ResponseEntity.badRequest().body("Sender or Receiver cannot be null");
        }
        try {
            Message savedMessage = messageService.sendMessage(message);
            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }
    // 获取接收者的所有消息
    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<List<Message>> getMessagesForReceiver(@PathVariable Long receiverId) {
        List<Message> messages = messageService.getMessagesForReceiver(receiverId);
        return ResponseEntity.ok(messages);
    }
    // 获取发送者的所有消息
    @GetMapping("/sender/{senderId}")
    public ResponseEntity<List<Message>> getMessagesForSender(@PathVariable Long senderId) {
        List<Message> messages = messageService.getMessagesForSender(senderId);
        return ResponseEntity.ok(messages);
    }
    //标记已读
    @PatchMapping("/{messageId}/read")
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long messageId) {
        try {
            messageService.markAsRead(messageId);
            return ResponseEntity.ok("Message marked as read");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to mark message as read: " + e.getMessage());
        }
    }
    //获取通讯录
    @GetMapping("/contacts/{userId}")
    public ResponseEntity<List<User>> getContacts(@PathVariable Long userId) {
        List<User> contacts = messageService.getContacts(userId);
        return ResponseEntity.ok(contacts);
    }
    //获取来往邮件
    @GetMapping("/conversation")
    public ResponseEntity<List<Message>> getConversation(
            @RequestParam Long userId,
            @RequestParam Long contactId) {
        List<Message> conversation = messageService.getConversation(userId, contactId);
        return ResponseEntity.ok(conversation);
    }
    //删除邮件
    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId,
            @RequestParam boolean isSender) {
        try {
            messageService.deleteMessage(messageId, userId, isSender);
            return ResponseEntity.ok("Message deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete message: " + e.getMessage());
        }
    }
}