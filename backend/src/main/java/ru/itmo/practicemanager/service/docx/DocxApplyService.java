package ru.itmo.practicemanager.service.docx;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import ru.itmo.practicemanager.entity.Student;
import ru.itmo.practicemanager.repository.UserRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocxApplyService {

    private final UserRepository userRepository;
    private final TemplateOptionConfig templateOptionConfig;
    private final DocxTemplateService templateService;

    public byte[] generatePracticeApplicationDocx(Long telegramId) {
        log.info("Генерация docx-отчёта для telegramId = {}", telegramId);
        ApplicationDocxData data = buildApplicationDocxData(telegramId);

        byte[] filled = fillDocxTemplate(data);
        log.info("Файл docx успешно сгенерирован для telegramId = {}", telegramId);
        return filled;
    }

    private byte[] fillDocxTemplate(ApplicationDocxData data) {
        Map<String, String> vars = buildVarsMap(data);
        log.debug("Карта подстановок: {}", vars.keySet());

        // берём копию шаблона из памяти
        byte[] templateCopy = templateService.getTemplateCopy();

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(templateCopy));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // замена в параграфах
            for (XWPFParagraph p : doc.getParagraphs()) {
                replaceInParagraph(p, vars);
            }
            // замена в ячейках таблиц
            for (XWPFTable tbl : doc.getTables()) {
                for (XWPFTableRow row : tbl.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            replaceInParagraph(p, vars);
                        }
                    }
                }
            }

            doc.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            log.error("Ошибка при заполнении docx-шаблона: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Ошибка при заполнении шаблона", ex);
        }
    }

    private ApplicationDocxData buildApplicationDocxData(Long telegramId) {
        log.info("Начато построение данных для telegramId={}", telegramId);

        Student student = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> {
                    log.error("Студент с telegramId={} не найден", telegramId);
                    return new IllegalArgumentException("Студент с telegramId " + telegramId + " не найден");
                }).getStudent();

        ApplicationDocxData data = ApplicationDocxData.builder()
                .fullName(student.getFullName())
                .group(student.getStudyGroup().getNumber())
                .practiceDates(templateOptionConfig.getPracticeDateRange())
                .faculty(student.getStudyGroup().getDirection().getFacultyName())
                .programCode(student.getStudyGroup().getDirection().getNumber())
                .programName(student.getStudyGroup().getDirection().getTranscript())
                .format(switch (student.getApply().getPracticeType()) {
                    case ONLINE -> "с применением дистанционных технологий";
                    case OFFLINE -> "очно";
                })
                .organization(student.getApply().getOrganization().getName())
                .representativeFullName(student.getApply().getSupervisor().getName())
                .build();

        log.info("Данные успешно построены для студента: {}", data.getFullName());
        return data;
    }

    private Map<String, String> buildVarsMap(ApplicationDocxData d) {
        Map<String, String> m = new HashMap<>();
        m.put("fullName", d.getFullName());
        m.put("faculty", d.getFaculty());
        m.put("programCode", d.getProgramCode());
        m.put("programName", d.getProgramName());
        m.put("group", d.getGroup());
        m.put("practiceDates", d.getPracticeDates());
        m.put("format", d.getFormat());
        m.put("organization", d.getOrganization());
        m.put("representativeFullName", d.getRepresentativeFullName());
        return m;
    }

    private void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> vars) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) return;

        StringBuilder raw = new StringBuilder();
        for (XWPFRun r : runs) {
            String t = r.getText(0);
            if (t != null) raw.append(t);
        }

        String replaced = replaceVars(raw.toString(), vars);
        if (replaced.equals(raw.toString())) return;

        for (int i = runs.size() - 1; i >= 0; i--) paragraph.removeRun(i);

        XWPFRun run = paragraph.createRun();
        run.setFontFamily("Times New Roman");
        run.setFontSize(12);

        String[] lines = replaced.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) run.addBreak();
            run.setText(lines[i], i);
        }
    }

    private String replaceVars(String text, Map<String, String> vars) {
        for (Map.Entry<String, String> e : vars.entrySet()) {
            text = text.replace("${" + e.getKey() + "}", e.getValue());
        }
        return text;
    }
}