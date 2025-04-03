package ru.itmo.practicemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.practicemanager.dto.DirectionDTO;
import ru.itmo.practicemanager.entity.Direction;
import ru.itmo.practicemanager.service.DirectionService;

import java.util.List;

@RestController
@RequestMapping("/api/directions")
@RequiredArgsConstructor
@CrossOrigin
public class DirectionController {

    private final DirectionService directionService;

    @GetMapping
    public ResponseEntity<List<Direction>> getAllDirections() {
        return ResponseEntity.ok(directionService.getAllDirections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Direction> getDirectionById(@PathVariable Long id) {
        return ResponseEntity.ok(directionService.getDirectionById(id));
    }

    @PostMapping
    public ResponseEntity<Direction> createDirection(@RequestBody DirectionDTO directionDTO) {
        return ResponseEntity.ok(directionService.createDirection(directionDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Direction> updateDirection(@PathVariable Long id, @RequestBody DirectionDTO updatedDirectionDTO) {
        return ResponseEntity.ok(directionService.updateDirection(id, updatedDirectionDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDirection(@PathVariable Long id) {
        directionService.deleteDirection(id);
        return ResponseEntity.noContent().build();
    }
}
