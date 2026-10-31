package de.voteeasy.export;

import de.voteeasy.invitation.UserToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

@Service
@Slf4j
@PreAuthorize("isAuthenticated()")
@Transactional
@Validated
public class UserTokenExcelService {

    public ResponseEntity<InputStreamResource> export(@NotNull Set<UserToken> tokens) throws IOException {
        StopWatch sw = new StopWatch();
        sw.start();

        ByteArrayInputStream in = downloadAsExcel(tokens);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=UserToken.xlsx");

        log.info("Generation of UserToken Excel took {} ms", sw.getDuration());

        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
    }

    public ByteArrayInputStream downloadAsExcel(@NotNull Set<UserToken> tokens) throws IOException {
        try (Workbook workbook = generateWorkbook(tokens); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private Workbook generateWorkbook(Set<UserToken> tokens) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        Sheet sheet = workbook.createSheet("UserToken");

        // style header row
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLUE_GREY.getIndex());
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Header Row itself
        Row header = sheet.createRow(0);
        header.setRowStyle(headerCellStyle);
        header.createCell(0).setCellValue("Token");
        header.createCell(1).setCellValue("CreatedAt");

        // style date cell
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("d.m.yyyy h:mm"));

        // Iterate over data elements
        if (tokens != null) {
            int startRowCount = 1;

            for (UserToken t : tokens) {
                Row row = sheet.createRow(startRowCount++);
                row.createCell(0).setCellValue(t.getToken());

                Cell date = row.createCell(1);
                date.setCellValue(Instant.ofEpochMilli(t.getCreatedAt()).atZone(ZoneId.systemDefault()).toLocalDateTime());
                date.setCellStyle(cellStyle);
            }
        } else {
            log.info("Exporting empty Excel without any user tokens.");
        }

        // make columns look nicer
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        return workbook;
    }
}
