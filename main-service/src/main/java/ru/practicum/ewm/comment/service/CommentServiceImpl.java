package ru.practicum.ewm.comment.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.QComment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.participation.ParticipationStatus;
import ru.practicum.ewm.participation.model.ParticipationRequest;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final ParticipationRepository requestRepository;

    @Override
    public CommentDto createComment(NewCommentDto newComment) {

        User user = userRepository.findById(newComment.getAuthorId()).get();
        if (user == null) {
            String bugText = "Пользователь не найден, id " + newComment.getAuthorId();
            log.warn(bugText);
            throw new NotFoundException(bugText);
        }

        Event event = eventRepository.findById(newComment.getEventId()).get();
        if (event == null) {
            String bugText = "Событие не найдено, id " + newComment.getEventId();
            log.warn(bugText);
            throw new NotFoundException(bugText);
        }

        if (!didUserParticipateEvent(user, event)) {
            String bugText = "Пользователь не участвовал в мероприятии " +
                    "либо заявка на участие не была одобрена, id " + newComment.getEventId();
            log.warn(bugText);
            throw new ValidationException(bugText);
        }

        Comment comment = CommentMapper.mapToComment(newComment);
        comment.setAuthor(user);
        comment.setEvent(event);

        comment = commentRepository.save(comment);

        return CommentMapper.mapToCommentDto(comment);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        User user = userRepository.findById(userId).get();
        if (user == null) {
            String bugText = "Пользователь не найден, id " + userId;
            log.warn(bugText);
            throw new NotFoundException(bugText);
        }

        Comment comment = commentRepository.findById(commentId).get();
        if (comment == null) {
            String bugText = "Комментарий не найден, id " + commentId;
            log.warn(bugText);
            throw new NotFoundException(bugText);
        }

        if (!comment.getAuthor().equals(user)) {
            String bugText = "Пользователь не модет удалять чужие комментарии";
            log.warn(bugText);
            throw new ValidationException(bugText);
        }

        commentRepository.delete(comment);

    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId).get();
        if (comment == null) {
            String bugText = "Комментарий не найден, id " + commentId;
            log.warn(bugText);
            throw new NotFoundException(bugText);
        }

        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDto> findCommentByEventId(Long eventId, LocalDateTime start, LocalDateTime end,
                                                 Integer from, Integer size) {

        Event event = eventRepository.findById(eventId).get();
        if (event == null) {
            String bugText = "Событие не найдено, id " + eventId;
            log.warn(bugText);
            throw new NotFoundException(bugText);
        }

        String logInfo = String.format("Поиск комментариев: eventId=%s, start=%s, end=%s, from=%s, size=%s",
                eventId, start, end, from, size);
        log.info(logInfo);

        if (start != null && end != null && start.isAfter(end)) {
            logInfo = "Дата окончания диапазона поиска не может быть раньше даты начала.";
            log.error(logInfo);
            throw new BadRequestException(logInfo);
        }

        BooleanExpression searchByParameters = QComment.comment.event.id.eq(eventId);

        if (start != null) {
            searchByParameters = QComment.comment.created.after(start);
        }

        if (end != null) {
            searchByParameters = searchByParameters.and(QComment.comment.created.before(end));
        }

        PageRequest pageable;

        pageable = PageRequest.of(from / size, size);

        Iterable<Comment> commentsIterable = commentRepository.findAll(searchByParameters, pageable);

        List<Comment> comments = new ArrayList<>();
        commentsIterable.forEach(comments::add);

        List<CommentDto> commentsDto = comments.stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());

        return commentsDto;
    }

    @Override
    public CommentDto findCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId).get();
        if (comment == null) {
            String bugText = "Комментарий не найден, id " + commentId;
            log.warn(bugText);
            throw new NotFoundException(bugText);
        }

        return CommentMapper.mapToCommentDto(comment);
    }


    public boolean didUserParticipateEvent(User user, Event event) {

        Optional<ParticipationRequest> participationRequest
                = requestRepository.findByRequesterIdAndEventId(user.getId(), event.getId());

        boolean result = true;
        if (participationRequest.isEmpty()
                || participationRequest.get().getStatus() != ParticipationStatus.CONFIRMED) {
            //Значит не было заявки для участия в мероприятии или заявку не подтвердили
            result = false;
        }

        return result;
    }
}
