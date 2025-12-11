package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private String logInfo;

    //Admin
    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {

        String email = newUserRequest.getEmail();
        if (userRepository.existsByEmail(email)) {
            logInfo = "Пользователь с таким email уже существует - " + email;
            log.error(logInfo);
            throw new ValidationException(logInfo);
        }

        User user = UserMapper.mapToUser(newUserRequest);
        User savedUser = userRepository.save(user);

        return UserMapper.mapToUserDto(savedUser);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {

        List<User> users;

        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(PageRequest.of(from / size, size)).getContent();
        } else {
            users = userRepository.findAllById(ids);
        }

        return users.stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public User getUserByIdWithException(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logInfo = "Пользователь с id " + userId + " не найден.";
                    log.error(logInfo);
                    return new NotFoundException(logInfo);
                });

        return user;
    }
}
