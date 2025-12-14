package ru.practicum.ewm.event.comments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.event.comments.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
