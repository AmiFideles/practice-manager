package ru.itmo.practicemanager.service.excel;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.practicemanager.entity.Student;
import ru.itmo.practicemanager.entity.StudyGroup;
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
        StudyGroup group = studyGroupRepository.findByNumber(groupNumber)
                .orElseGet(() -> studyGroupRepository.save(
                        StudyGroup.builder().number(groupNumber).build()));

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
}