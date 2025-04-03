package ru.itmo.practicemanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.practicemanager.dto.FacultyDTO;
import ru.itmo.practicemanager.entity.Faculty;
import ru.itmo.practicemanager.repository.FacultyRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;

    public List<Faculty> getAllFaculties() {
        return facultyRepository.findAll();
    }

    public Faculty getFacultyById(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Факультет с id " + id + " не найден"));
    }

    public Faculty createFaculty(FacultyDTO facultyDTO) {
        Faculty faculty = Faculty.builder()
                .number(facultyDTO.getNumber())
                .letter(facultyDTO.getLetter())
                .transcript(facultyDTO.getTranscript())
                .build();
        return facultyRepository.save(faculty);
    }

    public Faculty updateFaculty(Long id, FacultyDTO updatedFacultyDTO) {
        Faculty existingFaculty = facultyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Факультет с id " + id + " не найден"));

        existingFaculty.setNumber(updatedFacultyDTO.getNumber());
        existingFaculty.setLetter(updatedFacultyDTO.getLetter());
        existingFaculty.setTranscript(updatedFacultyDTO.getTranscript());

        return facultyRepository.save(existingFaculty);
    }

    public void deleteFaculty(Long id) {
        facultyRepository.deleteById(id);
    }
}
