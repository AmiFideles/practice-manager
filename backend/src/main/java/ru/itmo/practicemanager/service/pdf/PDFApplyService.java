package ru.itmo.practicemanager.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.stereotype.Service;
import ru.itmo.practicemanager.entity.Student;
import ru.itmo.practicemanager.repository.UserRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PDFApplyService {

    private static final String TEMPLATE_PATH = "src/main/resources/template.docx";
    private static final String PRACTICE_DATES = "с 10.03.2025 по 30.04.2025";

    private final UserRepository userRepository;

    private ApplicationPdfData buildApplicationPdfData(Long telegramId) {
        var builder = ApplicationPdfData.builder();
        Student student = userRepository.findById(telegramId).orElseThrow(() -> new IllegalArgumentException("Студент с telegramId " + telegramId + " не найден")).getStudent();
        builder.fullName(student.getFullName());
        builder.group(student.getStudyGroup().getNumber());
        builder.practiceDates(PRACTICE_DATES);
        builder.faculty(student.getStudyGroup().getDirection().getFacultyName());
        builder.programCode(student.getStudyGroup().getDirection().getNumber());
        builder.programName(student.getStudyGroup().getDirection().getTranscript());
        builder.format(
          switch (student.getApply().getPracticeType()) {
              case ONLINE -> "с применением дистанционных технологий";
              case OFFLINE -> "очно";
          }
        );
        builder.organization(student.getApply().getOrganization().getName());
        builder.organizationAddress(student.getApply().getOrganization().getLocation());
        builder.representativeFullName(student.getApply().getSupervisor().getName());
        builder.representativePosition("TODO");
        return builder.build();
    }



    public byte[] generatePracticeApplicationPdf(Long telegramId) {
        ApplicationPdfData data = buildApplicationPdfData(telegramId);

        byte[] filledDoc = fillDocxTemplate(data);
        return convertFromDocxToPdf(filledDoc);

    }

    /* ======================= CORE =========================================== */

    private byte[] fillDocxTemplate(ApplicationPdfData data) {
        Map<String, String> vars = buildVarsMap(data);

        try (InputStream templateIn = Files.newInputStream(Paths.get(TEMPLATE_PATH));
             XWPFDocument doc = new XWPFDocument(templateIn);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // параграфы документа
            doc.getParagraphs().forEach(p -> replaceInParagraph(p, vars));

            // параграфы в таблицах
            for (XWPFTable tbl : doc.getTables())
                for (XWPFTableRow row : tbl.getRows())
                    for (XWPFTableCell cell : row.getTableCells())
                        cell.getParagraphs().forEach(p -> replaceInParagraph(p, vars));

            doc.write(out);
            return out.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException("Ошибка при заполнении шаблона", ex);
        }
    }

    private byte[] convertFromDocxToPdf(byte[] docxFile) {
        try (InputStream in  = new ByteArrayInputStream(docxFile);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Загружаем DOCX из входного потока
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(in);
            Docx4J.toPDF(wordMLPackage, out);

            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка конвертации DOCX → PDF", e);
        }
    }


    /* ======================= HELPERS ======================================== */

    /**
     * Таблица замен: key -> value
     */
    private Map<String, String> buildVarsMap(ApplicationPdfData d) {
        Map<String, String> m = new HashMap<>();
        m.put("fullName", d.getFullName());
        m.put("faculty", d.getFaculty());
        m.put("programCode", d.getProgramCode());
        m.put("programName", d.getProgramName());
        m.put("group", d.getGroup());
        m.put("practiceDates", d.getPracticeDates());
        m.put("format", d.getFormat());
        m.put("organization", d.getOrganization());
        m.put("organizationAddress", d.getOrganizationAddress());
        m.put("representativePosition", d.getRepresentativePosition());
        m.put("representativeFullName", d.getRepresentativeFullName());
        return m;
    }

    private void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> vars) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) return;

        StringBuilder raw = new StringBuilder();
        for (XWPFRun r : runs) {
            String t = r.getText(0);
            if (t != null) raw.append(t);          // << пропускаем null
        }

        String replaced = replaceVars(raw.toString(), vars);
        if (replaced.equals(raw.toString())) return;   // ничего не поменялось

        // удаляем старые runs
        for (int i = runs.size() - 1; i >= 0; i--) paragraph.removeRun(i);

        // создаём один run с нужным стилем
        XWPFRun run = paragraph.createRun();
        run.setFontFamily("Times New Roman");
        run.setFontSize(12);

        // поддержим переносы строк, если они были
        String[] lines = replaced.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) run.addBreak();
            run.setText(lines[i], i);
        }
    }

    /**
     * Плоская ${key} → value
     */
    private String replaceVars(String text, Map<String, String> vars) {
        for (Map.Entry<String, String> e : vars.entrySet()) {
            text = text.replace("${" + e.getKey() + "}", e.getValue());
        }
        return text;
    }

}