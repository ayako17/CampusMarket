package com.backend.service;

import com.backend.entity.Note;
import com.backend.repository.NoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException; // 用于处理找不到ID的情况

@Service
public class NoteService {
    @Autowired
    private NoteMapper noteMapper;

    // 获取所有笔记
    public List<Note> getAllNotes() {
        return noteMapper.findAllNotes();
    }
      // 搜索笔记
    public List<Note> searchNotes(String keyword) {
        return noteMapper.findByTitleContaining(keyword);
    }

    // 根据用户ID获取笔记
    public List<Note> getNotesByUserId(Long userId) {
        return noteMapper.findByUserId(userId);
    }

    // 添加笔记
    @Transactional
    public Note addNote(Note note) {
        noteMapper.insertNote(note);
        Long noteId = note.getId();
        if (note.getImageUrls() != null && !note.getImageUrls().isEmpty()) {
            noteMapper.insertImageUrls(noteId, note.getImageUrls());
        }
        if (note.getAttachmentUrls() != null && !note.getAttachmentUrls().isEmpty()) {
            noteMapper.insertAttachmentUrls(noteId, note.getAttachmentUrls());
        }
        return note;
    }

    // 更新笔记
    @Transactional
    public Note updateNote(Note note) {
        Long noteId = note.getId();
        noteMapper.findById(noteId)
                .orElseThrow(() -> new NoSuchElementException("Note not found with id: " + noteId));
        noteMapper.updateNote(note);
        noteMapper.deleteImageUrlsByNoteId(noteId);
        if (note.getImageUrls() != null && !note.getImageUrls().isEmpty()) {
            noteMapper.insertImageUrls(noteId, note.getImageUrls());
        }
        noteMapper.deleteAttachmentUrlsByNoteId(noteId);
        if (note.getAttachmentUrls() != null && !note.getAttachmentUrls().isEmpty()) {
            noteMapper.insertAttachmentUrls(noteId, note.getAttachmentUrls());
        }
        return note;
    }

    // 删除笔记
    @Transactional
    public void deleteNote(Long noteId) {
        noteMapper.deleteImageUrlsByNoteId(noteId);
        noteMapper.deleteAttachmentUrlsByNoteId(noteId);
        noteMapper.deleteNoteById(noteId);
    }
}