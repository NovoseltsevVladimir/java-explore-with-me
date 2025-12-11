package ru.practicum.ewm.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    @Email(message = "Email должен быть корректным адресом электронной почты")
    @NotBlank(message = "Email не может быть пустым или состоять из пробелов")
    @Size(min = 6, max = 254, message = "Длина email должна быть от 6 до 254 символов")
    private String email;

    @NotBlank(message = "Имя не может быть пустым или состоять из пробелов")
    @Size(min = 2, max = 250, message = "Длина имени должна быть от 2 до 250 символов")
    private String name;
}
