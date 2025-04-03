package ru.itmo.practicemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.practicemanager.dto.DirectionDTO;
import ru.itmo.practicemanager.entity.Direction;
import ru.itmo.practicemanager.entity.Faculty;
import ru.itmo.practicemanager.repository.DirectionRepository;
import ru.itmo.practicemanager.repository.FacultyRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectionService {

    private final DirectionRepository directionRepository;
    private final FacultyRepository facultyRepository;

    public List<Direction> getAllDirections() {
        return directionRepository.findAll();
    }

    public Direction getDirectionById(Long id) {
        return directionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Направление с id " + id + " не найдено"));
    }

    public Direction createDirection(DirectionDTO directionDTO) {
        Faculty faculty = facultyRepository.findById(directionDTO.getFacultyId())
                .orElseThrow(() -> new RuntimeException("Факультет с id " + directionDTO.getFacultyId() + " не найден"));

        Direction direction = Direction.builder()
                .transcript(directionDTO.getTranscript())
                .faculty(faculty)
                .build();

        return directionRepository.save(direction);
    }

    public Direction updateDirection(Long id, DirectionDTO updatedDirectionDTO) {
        Direction existingDirection = directionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Направление с id " + id + " не найдено"));

        Faculty faculty = facultyRepository.findById(updatedDirectionDTO.getFacultyId())
                .orElseThrow(() -> new RuntimeException("Факультет с id " + updatedDirectionDTO.getFacultyId() + " не найден"));

        existingDirection.setTranscript(updatedDirectionDTO.getTranscript());
        existingDirection.setFaculty(faculty);

        return directionRepository.save(existingDirection);
    }

    public void deleteDirection(Long id) {
        directionRepository.deleteById(id);
    }
}
