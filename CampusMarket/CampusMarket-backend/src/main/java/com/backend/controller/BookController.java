package com.backend.controller;

import com.backend.entity.Book;
import com.backend.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:5173")
public class BookController {

    @Autowired
    private BookService bookService;
    //获取所有商品信息
    @GetMapping
    public ResponseEntity<?> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        Map<String, Object> response = new HashMap<>();
        response.put("books", books);
        response.put("message", "成功获取商品列表");
        return ResponseEntity.ok(response);
    }
    //添加一个商品
    @PostMapping
    public ResponseEntity<?> addBook(@RequestBody Book book) {
        try {
            Book savedBook = bookService.addBook(book);
            Map<String, Object> response = new HashMap<>();
            response.put("book", savedBook);
            response.put("message", "添加商品成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "添加商品失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    //根据关键字搜索商品
    @GetMapping("/search")
    public ResponseEntity<?> searchBooks(@RequestParam String keyword) {
        List<Book> books = bookService.searchBooks(keyword);
        Map<String, Object> response = new HashMap<>();
        response.put("books", books);
        response.put("message", "搜索成功");
        return ResponseEntity.ok(response);
    }
    //根据用户ID搜索商品
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBooksByUserId(@PathVariable Long userId) {
        List<Book> books = bookService.getBooksByUserId(userId);
        return ResponseEntity.ok(books);
    }
    //删除商品
    @DeleteMapping("/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable Long bookId) {
        try {
            bookService.deleteBook(bookId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Book deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete book: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}