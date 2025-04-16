package ru.itmo.practicemanager.service.excel;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.HashMap;
import java.util.Map;

public class ExcelStyleHelper {
    private final XSSFWorkbook workbook;
    private final Map<String, XSSFCellStyle> styles = new HashMap<>();

    public ExcelStyleHelper(XSSFWorkbook workbook) {
        this.workbook = workbook;
        initStyles();
    }

    private void initStyles() {
        styles.put("PURPLE", createStyle(new byte[]{(byte) 112, (byte) 48, (byte) 160})); // Not registered / fallback
        styles.put("BLUE", createStyle(new byte[]{(byte) 91, (byte) 155, (byte) 213}));   // ИТМО
        styles.put("GREEN", createStyle(new byte[]{(byte) 112, (byte) 173, (byte) 71}));    // Dark green

        styles.put("YELLOW", createStyle(new byte[]{(byte) 255, (byte) 192, (byte) 0})); // Waiting

        styles.put("FLAG_GREEN", createStyle(new byte[]{(byte) 112, (byte) 173, (byte) 71}));
        styles.put("FLAG_RED", createStyle(new byte[]{(byte) 255, (byte) 0, (byte) 0}));
        styles.put("FLAG_LIGHT_GREEN", createStyle(new byte[]{(byte) 197, (byte) 224, (byte) 180}));
        styles.put("GRAY", createStyle(new byte[]{(byte) 128, (byte) 128, (byte) 128}));
        styles.put("ORANGE", createStyle(new byte[]{(byte) 247, (byte) 150, (byte) 70})); // CREATED

    }

    private XSSFCellStyle createStyle(byte[] rgb) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(rgb, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    public XSSFCellStyle getStyle(String styleName) {
        return styles.get(styleName);
    }
}
