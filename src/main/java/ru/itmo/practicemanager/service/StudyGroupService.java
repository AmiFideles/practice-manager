package ru.itmo.practicemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.practicemanager.dto.StudyGroupDTO;
import ru.itmo.practicemanager.entity.StudyGroup;
import ru.itmo.practicemanager.entity.Direction;
import ru.itmo.practicemanager.repository.StudyGroupRepository;
import ru.itmo.practicemanager.repository.DirectionRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final DirectionRepository directionRepository;

    public List<StudyGroup> getAllGroups() {
        return studyGroupRepository.findAll();
    }

    public Optional<StudyGroup> getGroupById(Long id) {
        return studyGroupRepository.findById(id);
    }

    public StudyGroup createGroup(StudyGroupDTO studyGroupDTO) {
        Direction direction = directionRepository.findById(studyGroupDTO.getDirectionId())
                .orElseThrow(() -> new RuntimeException("Направление с id " + studyGroupDTO.getDirectionId() + " не найдено"));

        StudyGroup studyGroup = new StudyGroup();
        studyGroup.setNumber(studyGroupDTO.getNumber());
        studyGroup.setDirection(direction);

        return studyGroupRepository.save(studyGroup);
    }

    public StudyGroup updateGroup(Long id, StudyGroupDTO updatedGroupDTO) {
        StudyGroup existingGroup = studyGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Группа с id " + id + " не найдена"));

        Direction direction = directionRepository.findById(updatedGroupDTO.getDirectionId())
                .orElseThrow(() -> new RuntimeException("Направление с id " + updatedGroupDTO.getDirectionId() + " не найдено"));

        existingGroup.setNumber(updatedGroupDTO.getNumber());
        existingGroup.setDirection(direction);

        return studyGroupRepository.save(existingGroup);
    }

    public void deleteGroup(Long id) {
        studyGroupRepository.deleteById(id);
    }
}
