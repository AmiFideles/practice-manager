package ru.itmo.practicemanager.service.excel;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.practicemanager.entity.Direction;
import ru.itmo.practicemanager.entity.Student;
import ru.itmo.practicemanager.entity.StudyGroup;
import ru.itmo.practicemanager.repository.DirectionRepository;
import ru.itmo.practicemanager.repository.StudyGroupRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ParserService {
    private final StudyGroupRepository studyGroupRepository;
    private final DirectionRepository directionRepository;
    private static final String SOFTWARE_ENGINEERING = "09.03.04";
    private static final String COMPUTER_ENGINEERING = "09.03.01";

    public List<Student> parseStudentsFromExcel(MultipartFile file) {
        String html;
        try {
            html = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла", e);
        }

        Document doc = Jsoup.parse(html);
        List<Student> students = new ArrayList<>();

        String groupNumber = extractGroupNumber(doc);
        Direction direction = determineDirectionByGroupNumber(groupNumber);
        StudyGroup group = studyGroupRepository.findByNumber(groupNumber)
                .orElseGet(() -> {
                    StudyGroup newGroup = StudyGroup.builder()
                            .number(groupNumber)
                            .direction(direction)
                            .build();
                    return studyGroupRepository.save(newGroup);
                });

        if (group.getDirection() == null) {
            group.setDirection(direction);
            studyGroupRepository.save(group);
        }

        Elements rows = doc.select("table.c14 tr");

        for (int i = 1; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cols = row.select("td");

            if (cols.size() >= 3) {
                Student student = Student.builder()
                        .fullName(cols.get(2).text().trim())
                        .studyGroup(group)
                        .isuNumber(cols.get(1).text().trim())
                        .build();

                students.add(student);
            }
        }

        return students;
    }

    private String extractGroupNumber(Document doc) {
        Element title = doc.select("p.c1 span.c2").first();
        if (title != null) {
            String text = title.text();
            Pattern pattern = Pattern.compile("группы\\s+(\\S+)");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "Неизвестная группа";
    }

    private Direction determineDirectionByGroupNumber(String groupNumber) {
        if (groupNumber == null || groupNumber.length() < 3) {
            throw new IllegalArgumentException("Некорректный номер группы: " + groupNumber);
        }

        int startIndex = groupNumber.startsWith("P") ? 1 : 0;
        char thirdDigit = groupNumber.charAt(startIndex + 2);

        String directionNumber = (thirdDigit == '3') ? COMPUTER_ENGINEERING : SOFTWARE_ENGINEERING;

        return directionRepository.findByNumber(directionNumber)
                    .orElseThrow(() -> new EntityNotFoundException(
                        "Направление " + directionNumber + " не существует"));
    }
}