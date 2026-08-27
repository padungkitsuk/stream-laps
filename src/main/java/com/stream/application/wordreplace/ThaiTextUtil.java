package com.stream.application.wordreplace;

import java.util.Locale;
import com.ibm.icu.text.BreakIterator;

public class ThaiTextUtil {

    public static String addThaiWordBreakPreserveNewLine(String input) {

        String[] lines = input.split("\\r?\\n"); // ⭐ preserve paragraph
        StringBuilder finalResult = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            finalResult.append(processLine(lines[i]));

            if (i < lines.length - 1) {
                finalResult.append("\n"); // ⭐ keep original newline
            }
        }

        return finalResult.toString();
    }

    private static String processLine(String line) {
        BreakIterator boundary = BreakIterator.getLineInstance(new Locale("th", "TH"));
        boundary.setText(line);

        StringBuilder result = new StringBuilder();

        int start = boundary.first();
        int end = boundary.next();

        while (end != BreakIterator.DONE) {
            String word = line.substring(start, end);

            result.append(word).append("\u200B"); // allow wrap

            start = end;
            end = boundary.next();
        }

        return result.toString();
    }
}