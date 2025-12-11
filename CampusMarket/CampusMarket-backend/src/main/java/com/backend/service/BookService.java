package com.backend.service;

import com.backend.entity.Book;
import com.backend.repository.BookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BookService {
    @Autowired
    private BookMapper bookMapper;
    // 获取所有教材
    public List<Book> getAllBooks() {
        return bookMapper.findAllBooks();
    }
    // 添加/更新教材
    @Transactional
    public Book addBook(Book book) {
        if (book.getId() != null) {
             bookMapper.updateBook(book);
        } else {
            bookMapper.insertBook(book);
        }
        return book;
    }
    // 搜索教材
    public List<Book> searchBooks(String keyword) {
        return bookMapper.findByTitleContaining(keyword);
    }
    // 根据用户 ID 获取教材
    public List<Book> getBooksByUserId(Long userId) {
        return bookMapper.findByUserId(userId);
    }
    // 删除教材
    @Transactional
    public void deleteBook(Long bookId) {
        bookMapper.deleteBookById(bookId);
    }
}