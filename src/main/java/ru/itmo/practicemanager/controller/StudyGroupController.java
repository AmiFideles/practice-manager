package ru.itmo.practicemanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.practicemanager.dto.StudyGroupDTO;
import ru.itmo.practicemanager.entity.StudyGroup;
import ru.itmo.practicemanager.service.StudyGroupService;

import java.util.List;

@RestController
@RequestMapping("/api/study-groups")
@RequiredArgsConstructor
@CrossOrigin
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @GetMapping
    public List<StudyGroup> getAllGroups() {
        return studyGroupService.getAllGroups();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyGroup> getGroupById(@PathVariable Long id) {
        return studyGroupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StudyGroup> createGroup(@RequestBody StudyGroupDTO studyGroupDTO) {
        return ResponseEntity.ok(studyGroupService.createGroup(studyGroupDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyGroup> updateGroup(@PathVariable Long id, @RequestBody StudyGroupDTO updatedGroupDTO) {
        try {
            return ResponseEntity.ok(studyGroupService.updateGroup(id, updatedGroupDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        studyGroupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}
