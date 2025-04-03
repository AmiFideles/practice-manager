package ru.itmo.practicemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.practicemanager.dto.SupervisorDTO;
import ru.itmo.practicemanager.entity.Supervisor;
import ru.itmo.practicemanager.service.SupervisorService;

import java.util.List;

@RestController
@RequestMapping("/api/supervisors")
@RequiredArgsConstructor
@CrossOrigin
public class SupervisorController {

    private final SupervisorService supervisorService;

    @GetMapping
    public ResponseEntity<List<Supervisor>> getAllSupervisors() {
        return ResponseEntity.ok(supervisorService.getAllSupervisors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supervisor> getSupervisorById(@PathVariable Long id) {
        return supervisorService.getSupervisorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Supervisor> createSupervisor(@RequestBody SupervisorDTO supervisorDTO) {
        return ResponseEntity.ok(supervisorService.createSupervisor(supervisorDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supervisor> updateSupervisor(@PathVariable Long id, @RequestBody SupervisorDTO updatedSupervisorDTO) {
        try {
            return ResponseEntity.ok(supervisorService.updateSupervisor(id, updatedSupervisorDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupervisor(@PathVariable Long id) {
        supervisorService.deleteSupervisor(id);
        return ResponseEntity.noContent().build();
    }
}
