package com.budaassociacao.legendas;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for DOCX files to extract subtitles/captions
 * Subtitles are separated by blank lines
 */
public class DocxParser {

    /**
     * Extract legendas from a DOCX file
     * Legendas are separated by one or more blank lines
     *
     * @param file DOCX file to parse
     * @return List of legendas (captions)
     * @throws Exception if parsing fails
     */
    public List<String> extractLegendas(File file) throws Exception {
        List<String> legendas = new ArrayList<>();
        StringBuilder currentLegenda = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();

            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText().trim();

                if (text.isEmpty()) {
                    // Blank line - save current legenda if not empty
                    if (currentLegenda.length() > 0) {
                        legendas.add(currentLegenda.toString().trim());
                        currentLegenda = new StringBuilder();
                    }
                } else {
                    // Non-blank line - add to current legenda
                    if (currentLegenda.length() > 0) {
                        currentLegenda.append("\n");
                    }
                    currentLegenda.append(text);
                }
            }

            // Add last legenda if not empty
            if (currentLegenda.length() > 0) {
                legendas.add(currentLegenda.toString().trim());
            }
        }

        return legendas;
    }
}
