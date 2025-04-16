package ru.itmo.practicemanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.practicemanager.dto.RoleDto;
import ru.itmo.practicemanager.dto.UserDto;
import ru.itmo.practicemanager.entity.Role;
import ru.itmo.practicemanager.entity.User;
import ru.itmo.practicemanager.repository.UserRepository;
import ru.itmo.practicemanager.service.StudentService;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final StudentService studentService;
    private final UserRepository userRepository;

    @Operation(summary = "Зарегистрироваться в боте")
    @PostMapping("/register")
    public ResponseEntity<String> registerStudent(
            @RequestBody UserDto request) {
        if (userRepository.findByTelegramIdAndRole(request.getTelegramId(), Role.ADMIN).isPresent()){
            return ResponseEntity.ok("Вам доступны команды администратора");
        }
        return ResponseEntity.ok("Запрос на регистрацию отправлен. Вы можете проверить статус с помощью ...");
    }

    @Operation(summary = "Получить роль юзера по его TG id")
    @GetMapping("/role")
    public ResponseEntity<RoleDto> getRole(@RequestParam Long telegramId) {
        Optional<User> byTelegramUsername =
                userRepository.findByTelegramId(telegramId);
        return byTelegramUsername.map(user -> ResponseEntity.ok(new RoleDto(user.getRole().name())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
