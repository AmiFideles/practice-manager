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
        styles.put("PURPLE", createStyle(new byte[]{(byte) 200, (byte) 160, (byte) 220}));
        styles.put("BLUE", createStyle(new byte[]{(byte) 173, (byte) 216, (byte) 230}));
        styles.put("GREEN", createStyle(new byte[]{(byte) 144, (byte) 238, (byte) 144}));

        styles.put("FLAG_GREEN", createStyle(new byte[]{(byte) 0, (byte) 255, (byte) 0}));
        styles.put("FLAG_RED", createStyle(new byte[]{(byte) 255, (byte) 99, (byte) 71}));
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
