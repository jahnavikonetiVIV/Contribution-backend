package com.jahnavi.contribution.contribution_management.helper;

import com.jahnavi.contribution.contribution_management.dto.EcollectTransactionDto;
import com.vivriti.investron.common.exception.CoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MisReportParserHelper {


    public List<EcollectTransactionDto> parseFile(InputStream is, String fileName) {
        if (fileName == null) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "File name missing");
        }

        fileName = fileName.toLowerCase();
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))
            return parseExcel(is);
        if (fileName.endsWith(".csv"))
            return parseCsv(is);
        throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Unsupported file type");
    }

    private List<EcollectTransactionDto> parseExcel(InputStream is) {

        List<EcollectTransactionDto> list = new ArrayList<>();

        try (Workbook wb = WorkbookFactory.create(is)) {

            Sheet sheet = wb.getSheetAt(0);
            int rowNo = 0;

            for (Row row : sheet) {
                rowNo++;

                if (rowNo == 1 || isRowEmpty(row)) {
                    continue;
                }

                processExcelRow(row, rowNo, list);
            }

        } catch (Exception e) {
            throw new CoreException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Excel parsing failed: " + e.getMessage(),
                    e
            );
        }

        return list;
    }

    private void processExcelRow(Row row,
                                 int rowNo,
                                 List<EcollectTransactionDto> list) {
        EcollectTransactionDto dto = parseExcelRow(row, rowNo);
        list.add(dto);
    }


    private EcollectTransactionDto parseExcelRow(Row row, int rowNo) {

        EcollectTransactionDto dto = new EcollectTransactionDto();

        dto.setTransferUniqueNo(getCellValue(row.getCell(6)));


        Cell dateCell = row.getCell(7);
        if (dateCell != null &&
                dateCell.getCellType() == CellType.NUMERIC &&
                DateUtil.isCellDateFormatted(dateCell)) {

            dto.setTransferTimestamp(dateCell.getLocalDateTimeCellValue());
        } else {
            dto.setTransferTimestamp(parseDate(getCellValue(dateCell)));
        }

        dto.setTransferAmt(getCellValue(row.getCell(8)));

        dto.setVirtualAccountNo(getCellValue(row.getCell(0)));
        dto.setRmtrFullName(getCellValue(row.getCell(3)));
        dto.setRmtrAccountNo(getCellValue(row.getCell(4)));
        dto.setRmtrAccountIfsc(getCellValue(row.getCell(5)));

        dto.setTransferType(getCellValue(row.getCell(11)));
        dto.setStatus(getCellValue(row.getCell(12)));

        if (dto.getTransferUniqueNo() == null || dto.getTransferAmt() == null) {
            throw new CoreException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Mandatory fields missing at row " + rowNo
            );
        }

        return dto;
    }


    private List<EcollectTransactionDto> parseCsv(InputStream is) {

        List<EcollectTransactionDto> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line;
            int row = 0;

            while ((line = br.readLine()) != null) {
                row++;
                if (row == 1) {
                    continue;
                }

                processCsvLine(line, list);
            }

        } catch (Exception e) {
            throw new CoreException(
                    HttpStatus.BAD_REQUEST.value(),
                    "CSV parsing failed: " + e.getMessage(),
                    e
            );
        }

        return list;
    }

    private void processCsvLine(String line,
                                List<EcollectTransactionDto> list) {

        String[] col = safeCsvSplit(line);

        EcollectTransactionDto dto = new EcollectTransactionDto();
        dto.setCustomerCode(col[0]);
        dto.setTransferUniqueNo(col[1]);
        dto.setTransferAmt(col[3]);
        dto.setTransferTimestamp(parseDate(col[4]));

        list.add(dto);
    }


    private String[] safeCsvSplit(String line) {

        List<String> cols = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                inQuotes = !inQuotes; // toggle quote state
            } else if (ch == ',' && !inQuotes) {
                cols.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        cols.add(current.toString().trim());
        return cols.toArray(new String[0]);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;

        DataFormatter formatter = new DataFormatter();

        switch (cell.getCellType()) {

            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Date handling (already handled separately in your code,
                    // but keeping safe fallback here)
                    LocalDateTime dateTime = cell.getLocalDateTimeCellValue();
                    return dateTime.toString();
                } else {
                    // Prevent scientific notation and precision loss display
                    return formatter.formatCellValue(cell).trim();
                }

            case FORMULA:
                // Evaluate formula and return formatted value
                return formatter.formatCellValue(cell).trim();

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            default:
                return null;
        }
    }




    private LocalDateTime parseDate(String val) {
        if (val == null || val.isBlank()) return null;

        val = val
                .replace('\u00A0', ' ')
                .replace("\u200B", "")
                .replaceAll("\\p{C}", "")
                .trim();

        String[] formats = {
                "yyyy-MM-dd HH:mm:ss",
                "dd-MM-yyyy HH:mm:ss",
                "MM/dd/yyyy HH:mm:ss",
                "MM/dd/yyyy hh:mm:ss a"
        };

        for (String f : formats) {
            try {
                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern(f, Locale.ENGLISH);

                return LocalDateTime.parse(val, formatter);
            } catch (Exception ignored) {
                // try next format
            }
        }

        throw new CoreException(
                HttpStatus.BAD_REQUEST.value(),
                "Unparseable date value: [" + val + "]"
        );
    }



    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell c = row.getCell(i);
            if (c != null && c.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}
