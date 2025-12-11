package com.backend.repository;

import com.backend.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface NoteMapper {

    List<Note> findAllNotes();

    void insertNote(Note note);
    int updateNote(Note note);

    Optional<Note> findById(@Param("id") Long id);

    List<Note> findByTitleContaining(@Param("keyword") String keyword);

    List<Note> findByUserId(@Param("userId") Long userId);

    void deleteNoteById(@Param("id") Long id);
    void insertImageUrls(@Param("noteId") Long noteId, @Param("urls") List<String> urls);

    void insertAttachmentUrls(@Param("noteId") Long noteId, @Param("urls") List<String> urls);

    void deleteImageUrlsByNoteId(@Param("noteId") Long noteId);

    void deleteAttachmentUrlsByNoteId(@Param("noteId") Long noteId);

    List<String> findImageUrlsByNoteId(@Param("noteId") Long noteId);

    List<String> findAttachmentUrlsByNoteId(@Param("noteId") Long noteId);
}