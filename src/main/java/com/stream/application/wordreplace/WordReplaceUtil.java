package com.stream.application.wordreplace;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class WordReplaceUtil {

    public void replaceTextWithNewLine(XWPFDocument doc, String target, String replacement) {
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            replaceInParagraph(paragraph, target, replacement);
        }

        // รองรับ text ใน table ด้วย
        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        replaceInParagraph(paragraph, target, replacement);
                    }
                }
            }
        }
    }
    
    private void replaceInParagraph(XWPFParagraph paragraph, String target, String replacement) {
        for (int i = 0; i < paragraph.getRuns().size(); i++) {
            XWPFRun run = paragraph.getRuns().get(i);
            String text = run.getText(0);

            if (text != null && text.contains(target)) {
                run.setText("", 0); // clear ของเดิม

                String[] lines = replacement.split("\n");

                for (int j = 0; j < lines.length; j++) {
                    if (j > 0) {
                        run.addBreak(); // 🔥 newline ใน Word
                    }
                    run.setText(lines[j], j);
                }
            }
        }
    }
    
    // replace image in table 
    public void replaceImageInTable(XWPFDocument document,String placeholder, String imagePath, int width, int height) throws Exception {
        for (XWPFTable table : document.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    replaceImageInCell(
                            cell,
                            placeholder,
                            imagePath,
                            width,
                            height
                    );

                    // รองรับ nested table
                    for (XWPFTable nestedTable : cell.getTables()) {
                        for (XWPFTableRow nestedRow : nestedTable.getRows()) {
                            for (XWPFTableCell nestedCell : nestedRow.getTableCells()) {
                                replaceImageInCell(
                                        nestedCell,
                                        placeholder,
                                        imagePath,
                                        width,
                                        height
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    private void replaceImageInCell(
            XWPFTableCell cell,
            String placeholder,
            String imagePath,
            int width,
            int height
    ) throws Exception {
        List<XWPFParagraph> paragraphs = cell.getParagraphs();
        for (XWPFParagraph paragraph : paragraphs) {
            String text = paragraph.getText();
            if (text != null && text.contains(placeholder)) {
                // remove all runs
                List<XWPFRun> runs = paragraph.getRuns();
                for (int i = runs.size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }
                // add image
                XWPFRun imageRun = paragraph.createRun();
                try (InputStream is = new FileInputStream(imagePath)) {
                    imageRun.addPicture(
                            is,
                            getPictureType(imagePath),
                            imagePath,
                            Units.toEMU(width),
                            Units.toEMU(height)
                    );
                }

                // center align
                paragraph.setAlignment(ParagraphAlignment.CENTER);

                return;
            }
        }
    }

    private int getPictureType(String fileName) {
        String lower = fileName.toLowerCase();

        if (lower.endsWith(".png")) {
            return Document.PICTURE_TYPE_PNG;
        }
        if (lower.endsWith(".jpg")|| lower.endsWith(".jpeg")) {
            return Document.PICTURE_TYPE_JPEG;
        }
        if (lower.endsWith(".gif")) {
            return Document.PICTURE_TYPE_GIF;
        }
        if (lower.endsWith(".bmp")) {
            return Document.PICTURE_TYPE_BMP;
        }
        throw new RuntimeException(
                "Unsupported image type: " + fileName
        );
    }
}
