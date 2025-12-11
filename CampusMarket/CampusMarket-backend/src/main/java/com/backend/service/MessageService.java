package com.backend.service;

import com.backend.entity.Message;
import com.backend.entity.User;
import com.backend.repository.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;
    // 发送消息
    @Transactional
    public Message sendMessage(Message message) {
        messageMapper.insertMessage(message);
        return message;
    }
    // 获取接收者的所有消息
    public List<Message> getMessagesForReceiver(Long receiverId) {
        return messageMapper.findByReceiverIdOrderByTimestampDesc(receiverId);
    }
    // 获取发送者的所有消息
    public List<Message> getMessagesForSender(Long senderId) {
        return messageMapper.findBySenderIdOrderByTimestampDesc(senderId);
    }
    //标记已读
    @Transactional
    public void markAsRead(Long messageId) {
        Message message = messageMapper.findById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("Message not found");
        }
        message.setIsRead(true);
        messageMapper.updateMessage(message);
    }
    // 获取通讯录
    public List<User> getContacts(Long userId) {
        List<User> sentContacts = messageMapper.findDistinctReceiverBySenderId(userId);
        List<User> receivedContacts = messageMapper.findDistinctSenderByReceiverId(userId);
        Set<User> contacts = new HashSet<>(sentContacts);
        contacts.addAll(receivedContacts);
        return new ArrayList<>(contacts);
    }
    // 获取与特定用户的来往邮件
    public List<Message> getConversation(Long userId, Long contactId) {
        return messageMapper.findConversationBetweenUsers(userId, contactId);
    }
    //删除邮件
    @Transactional
    public void deleteMessage(Long messageId, Long userId, boolean isSender) {
        Message message = messageMapper.findById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("Message not found");
        }

        if (isSender) {
            if (message.getSender() == null || !message.getSender().getId().equals(userId)) {
                throw new IllegalArgumentException("User is not the sender of this message");
            }
            message.setIsDeletedBySender(true);
        } else {
            if (message.getReceiver() == null || !message.getReceiver().getId().equals(userId)) {
                throw new IllegalArgumentException("User is not the receiver of this message");
            }
            message.setIsDeletedByReceiver(true);
        }
        // 如果双方都删除，则从数据库中移除
        if (message.getIsDeletedBySender() && message.getIsDeletedByReceiver()) {
            messageMapper.deleteMessage(message.getId());
        } else {
            messageMapper.updateMessage(message);
        }
    }
}