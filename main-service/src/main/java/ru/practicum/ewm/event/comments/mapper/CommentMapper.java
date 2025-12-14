package ru.practicum.ewm.event.comments.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.comments.dto.CommentDto;
import ru.practicum.ewm.event.comments.dto.NewCommentDto;
import ru.practicum.ewm.event.comments.model.Comment;

import java.time.LocalDateTime;

@Component
public class CommentMapper {

    public static Comment mapToComment(NewCommentDto request) {

        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setCreated(LocalDateTime.now());

        return comment;
    }

    public static CommentDto mapToCommentDto(Comment comment) {

        CommentDto dto = new CommentDto();
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setId(comment.getId());
        dto.setEventId(comment.getEvent().getId());
        dto.setText(comment.getText());
        dto.setCreated(comment.getCreated());

        return dto;
    }

}
