package com.example.app_gestion_presences.service;

import com.example.app_gestion_presences.entity.Attendance;
import com.example.app_gestion_presences.entity.AttendanceStatus;
import com.example.app_gestion_presences.entity.Event;
import com.example.app_gestion_presences.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void exportPresences(Event seance, List<Attendance> attendances,
                                 HttpServletResponse response) throws IOException {

        String filename = "presences_" + seance.getId() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Feuille de présence");

            // ── Styles ───────────────────────────────────────────────────────
            CellStyle titleStyle = makeTitleStyle(wb);
            CellStyle headerStyle = makeHeaderStyle(wb);
            CellStyle presentStyle = makeDataStyle(wb, new int[]{0xE6, 0xF7, 0xEE}, new int[]{0x1B, 0x9C, 0x5B});
            CellStyle retardStyle  = makeDataStyle(wb, new int[]{0xFD, 0xF5, 0xE6}, new int[]{0xC2, 0x84, 0x1B});
            CellStyle absentStyle  = makeDataStyle(wb, new int[]{0xFC, 0xE8, 0xE8}, new int[]{0xC0, 0x39, 0x2B});
            CellStyle totalStyle   = makeTotalStyle(wb);
            CellStyle labelStyle   = makeLabelStyle(wb);

            int row = 0;

            // ── Ligne titre ───────────────────────────────────────────────────
            Row titleRow = sheet.createRow(row++);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            String titreSeance = seance.getTitle() != null ? seance.getTitle() : "Séance";
            String promoGroupe = seance.getPromotion().getName()
                    + (seance.getGroup() != null ? " · " + seance.getGroup().getName() : "");
            String dateStr = seance.getStartTime() != null
                    ? seance.getStartTime().format(FMT_DATE) : "—";
            titleCell.setCellValue("Feuille de présence — " + titreSeance + " | " + promoGroupe + " | " + dateStr);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // ── Ligne enseignant ──────────────────────────────────────────────
            Row infoRow = sheet.createRow(row++);
            Cell infoCell = infoRow.createCell(0);
            infoCell.setCellValue("Enseignant : " + seance.getEnseignant().getFirstname()
                    + " " + seance.getEnseignant().getLastname()
                    + "    Seuil retard : " + seance.getLateThreshold() + " min");
            infoCell.setCellStyle(labelStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

            row++; // ligne vide

            // ── En-têtes colonnes ─────────────────────────────────────────────
            String[] headers = {"Nom", "Prénom", "Email", "Groupe", "Statut", "Heure", "Retard (min)"};
            Row headerRow = sheet.createRow(row++);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // ── Données ───────────────────────────────────────────────────────
            long nbPresent = 0, nbRetard = 0, nbAbsent = 0;
            for (Attendance att : attendances) {
                User u = att.getUser();
                CellStyle rowStyle;
                String statutLabel;

                if (att.getStatus() == AttendanceStatus.Present) {
                    rowStyle = presentStyle; statutLabel = "Présent"; nbPresent++;
                } else if (att.getStatus() == AttendanceStatus.Late) {
                    rowStyle = retardStyle; statutLabel = "Retard"; nbRetard++;
                } else {
                    rowStyle = absentStyle; statutLabel = "Absent"; nbAbsent++;
                }

                Row dataRow = sheet.createRow(row++);
                dataRow.setHeightInPoints(18);
                setCells(dataRow, rowStyle,
                        u.getLastname(),
                        u.getFirstname(),
                        u.getEmail(),
                        u.getGroup() != null ? u.getGroup().getName() : "—",
                        statutLabel,
                        att.getValidationTime() != null ? att.getValidationTime().format(FMT_TIME) : "—",
                        att.getLateMinutes() > 0 ? String.valueOf(att.getLateMinutes()) : "—"
                );
            }

            row++; // ligne vide

            // ── Récapitulatif ─────────────────────────────────────────────────
            int total = attendances.size();
            int taux  = total > 0 ? (int) Math.round((nbPresent + nbRetard) * 100.0 / total) : 0;

            addSummaryRow(sheet, row++, totalStyle, "Total étudiants", String.valueOf(total));
            addSummaryRow(sheet, row++, totalStyle, "Présents",  nbPresent + " (" + (total > 0 ? Math.round(nbPresent * 100.0 / total) : 0) + "%)");
            addSummaryRow(sheet, row++, totalStyle, "Retards",   nbRetard  + " (" + (total > 0 ? Math.round(nbRetard  * 100.0 / total) : 0) + "%)");
            addSummaryRow(sheet, row++, totalStyle, "Absents",   nbAbsent  + " (" + (total > 0 ? Math.round(nbAbsent  * 100.0 / total) : 0) + "%)");
            addSummaryRow(sheet, row,   totalStyle, "Taux de présence", taux + "%");

            // ── Largeurs colonnes ─────────────────────────────────────────────
            int[] widths = {5000, 5000, 9000, 4000, 3500, 4000, 3500};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i]);
            }

            wb.write(response.getOutputStream());
        }
    }

    // ── Helpers styles ────────────────────────────────────────────────────────

    private CellStyle makeTitleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 13);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        return s;
    }

    private CellStyle makeHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s, BorderStyle.MEDIUM, IndexedColors.WHITE.getIndex());
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 10);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        return s;
    }

    private CellStyle makeDataStyle(Workbook wb, int[] bgRgb, int[] fgRgb) {
        CellStyle s = wb.createCellStyle();
        // IndexedColors ne suffit pas pour les couleurs custom → on utilise les indices proches
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT.getIndex());
        Font f = wb.createFont();
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        return s;
    }

    private CellStyle makeTotalStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.LEFT);
        setBorder(s, BorderStyle.THIN, IndexedColors.WHITE.getIndex());
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        return s;
    }

    private CellStyle makeLabelStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        Font f = wb.createFont();
        f.setItalic(true);
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        return s;
    }

    private void setBorder(CellStyle s, BorderStyle bs, short color) {
        s.setBorderTop(bs);    s.setTopBorderColor(color);
        s.setBorderBottom(bs); s.setBottomBorderColor(color);
        s.setBorderLeft(bs);   s.setLeftBorderColor(color);
        s.setBorderRight(bs);  s.setRightBorderColor(color);
    }

    private void setCells(Row row, CellStyle style, String... values) {
        for (int i = 0; i < values.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(values[i]);
            c.setCellStyle(style);
        }
    }

    private void addSummaryRow(Sheet sheet, int rowNum, CellStyle style, String label, String value) {
        Row r = sheet.createRow(rowNum);
        r.setHeightInPoints(18);
        Cell lbl = r.createCell(0);
        lbl.setCellValue(label);
        lbl.setCellStyle(style);
        Cell val = r.createCell(1);
        val.setCellValue(value);
        val.setCellStyle(style);
    }
}
