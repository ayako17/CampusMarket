package com.backend.repository;

import com.backend.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BookMapper {

    List<Book> findAllBooks();

    Optional<Book> findById(@Param("id") Long id);

    void insertBook(Book book);

    int updateBook(Book book);

    List<Book> findByTitleContaining(@Param("keyword") String keyword);

    List<Book> findByUserId(@Param("userId") Long userId);

    void deleteBookById(@Param("id") Long id);
}