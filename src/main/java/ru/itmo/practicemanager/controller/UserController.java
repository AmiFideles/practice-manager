package ru.itmo.practicemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.practicemanager.dto.RoleDto;
import ru.itmo.practicemanager.dto.UserDto;
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

    @PostMapping("/register")
    public ResponseEntity<String> registerStudent(
            @RequestBody UserDto request) {
        studentService.register(request);
        return ResponseEntity.ok("Запрос на регистрацию отправлен. Вы можете проверить статус с помощью ...");
    }

    @GetMapping("/role")
    public ResponseEntity<RoleDto> getRole(@RequestParam String tgUsername) {
        Optional<User> byTelegramUsername =
                userRepository.findByTelegramUsername(tgUsername);
        return byTelegramUsername.map(user -> ResponseEntity.ok(new RoleDto(user.getRole().name())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
