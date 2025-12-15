package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.MainServiceConstants.DATA_DTO_PATTERN;

@RestController
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService commentService;

    //Private
    @PostMapping("/users/{userId}/events/{eventId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@Valid @RequestBody NewCommentDto newComment,
                                    @PathVariable("userId") Long userId,
                                    @PathVariable("eventId") Long eventId) {

        newComment.setCreated(LocalDateTime.now());
        newComment.setEventId(eventId);
        newComment.setAuthorId(userId);

        return commentService.createComment(newComment);
    }

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable("userId") Long userId,
                              @PathVariable("commentId") Long commentId) {
        commentService.deleteComment(userId, commentId);
    }

    //Public
    @GetMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> findCommentByEventId(@PathVariable("eventId") Long eventId,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = DATA_DTO_PATTERN) LocalDateTime start,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = DATA_DTO_PATTERN) LocalDateTime end,
                                                 @RequestParam(required = false, defaultValue = "0") Integer from,
                                                 @RequestParam(required = false, defaultValue = "10") Integer size) {
        return commentService.findCommentByEventId(eventId, start, end, from, size);
    }

    @GetMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto findCommentById(@PathVariable("commentId") Long commentId) {
        return commentService.findCommentById(commentId);
    }

    //Admin
    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable("commentId") Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }

}
