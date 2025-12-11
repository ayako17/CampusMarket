package com.backend.repository;

import com.backend.entity.Message;
import com.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    void insertMessage(Message message);

    int updateMessage(Message message);

    int deleteMessage(Long id);

    Message findById(@Param("id") Long id);

    List<Message> findByReceiverIdOrderByTimestampDesc(@Param("receiverId") Long receiverId);

    List<Message> findBySenderIdOrderByTimestampDesc(@Param("senderId") Long senderId);

    List<User> findDistinctReceiverBySenderId(@Param("userId") Long userId);

    List<User> findDistinctSenderByReceiverId(@Param("userId") Long userId);

    List<Message> findConversationBetweenUsers(
            @Param("userId") Long userId,
            @Param("contactId") Long contactId);
}