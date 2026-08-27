package com.stream.application.wordreplace;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.icu.text.BreakIterator;

public class ThaiTextUtil {

    private static final String ZWSP = "​";
    // Thin space: has real, measurable width (unlike ZWSP) so it can be repeated to pad a
    // line out to a target width for justification.
    private static final String THIN_SPACE = " ";
    private static final String FONT_RESOURCE = "THSarabunNew.ttf";
    private static final Map<Float, Font> FONT_CACHE = new ConcurrentHashMap<>();

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

    /**
     * Same word-break behaviour as {@link #addThaiWordBreakPreserveNewLine(String)}, but every
     * wrapped line (except the last line of a paragraph, and any line with only one segment)
     * is right-justified to {@code widthPoints} by padding the gaps between ICU word-break
     * segments with thin spaces, sized using the real glyph metrics of {@code fontSizePt} pt
     * TH Sarabun. Thai has no natural inter-word space to stretch (unlike Latin text), so this
     * measures and pads manually instead of relying on JasperReports' own Justified alignment,
     * which has no effect on text built from {@link #addThaiWordBreakPreserveNewLine(String)}.
     *
     * @param widthPoints the text field's usable width in points (box width minus padding/indent)
     */
    public static String addThaiWordBreakJustified(String input, float widthPoints, float fontSizePt) {
        Font font = font(fontSizePt);
        FontRenderContext frc = new FontRenderContext(null, true, true);

        String[] lines = input.split("\\r?\\n");
        StringBuilder finalResult = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            finalResult.append(justifyLine(lines[i], widthPoints, font, frc));

            if (i < lines.length - 1) {
                finalResult.append("\n");
            }
        }

        return finalResult.toString();
    }

    private static String justifyLine(String line, float widthPoints, Font font, FontRenderContext frc) {
        List<String> segments = segment(line);
        List<List<String>> wrapped = wrap(segments, widthPoints, font, frc);

        StringBuilder out = new StringBuilder();
        for (int li = 0; li < wrapped.size(); li++) {
            List<String> subLine = wrapped.get(li);
            boolean lastSubLine = (li == wrapped.size() - 1);
            out.append(lastSubLine || subLine.size() < 2
                    ? joinUnjustified(subLine)
                    : joinJustified(subLine, widthPoints, font, frc));
        }
        return out.toString();
    }

    private static List<String> segment(String line) {
        BreakIterator boundary = BreakIterator.getLineInstance(new Locale("th", "TH"));
        boundary.setText(line);

        List<String> segments = new ArrayList<>();
        int start = boundary.first();
        int end = boundary.next();
        while (end != BreakIterator.DONE) {
            segments.add(line.substring(start, end));
            start = end;
            end = boundary.next();
        }
        return segments;
    }

    private static List<List<String>> wrap(List<String> segments, float widthPoints, Font font, FontRenderContext frc) {
        List<List<String>> wrapped = new ArrayList<>();
        List<String> current = new ArrayList<>();
        float currentWidth = 0f;

        for (String seg : segments) {
            float segWidth = width(seg, font, frc);
            if (!current.isEmpty() && currentWidth + segWidth > widthPoints) {
                wrapped.add(current);
                current = new ArrayList<>();
                currentWidth = 0f;
            }
            current.add(seg);
            currentWidth += segWidth;
        }
        if (!current.isEmpty()) {
            wrapped.add(current);
        }
        return wrapped;
    }

    private static String joinUnjustified(List<String> segments) {
        StringBuilder out = new StringBuilder();
        for (String seg : segments) {
            out.append(seg).append(ZWSP);
        }
        return out.toString();
    }

    private static String joinJustified(List<String> segments, float widthPoints, Font font, FontRenderContext frc) {
        float rawWidth = 0f;
        for (String seg : segments) {
            rawWidth += width(seg, font, frc);
        }

        float slack = Math.max(0f, widthPoints - rawWidth);
        int gaps = segments.size() - 1;
        float thinSpaceWidth = width(THIN_SPACE, font, frc);
        int totalThinSpaces = thinSpaceWidth > 0f ? Math.round(slack / thinSpaceWidth) : 0;

        // Spread totalThinSpaces evenly across the gaps using running cumulative targets
        // (like Bresenham line drawing), instead of rounding slack/gaps per gap independently -
        // that loses the whole budget to underflow whenever the per-gap share is smaller than
        // one thin space, which is the common case once a line already nearly fills the width.
        StringBuilder out = new StringBuilder();
        int distributed = 0;
        for (int i = 0; i < segments.size(); i++) {
            out.append(segments.get(i));
            if (i < gaps) {
                int cumulativeTarget = (i + 1) * totalThinSpaces / gaps;
                int count = cumulativeTarget - distributed;
                distributed = cumulativeTarget;
                out.append(ZWSP);
                for (int t = 0; t < count; t++) {
                    out.append(THIN_SPACE);
                }
            }
        }
        return out.toString();
    }

    private static float width(String text, Font font, FontRenderContext frc) {
        return (float) font.getStringBounds(text, frc).getWidth();
    }

    private static Font font(float sizePt) {
        return FONT_CACHE.computeIfAbsent(sizePt, size -> {
            try (InputStream is = ThaiTextUtil.class.getClassLoader().getResourceAsStream(FONT_RESOURCE)) {
                if (is == null) {
                    throw new IOException("Font resource not found: " + FONT_RESOURCE);
                }
                return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
            } catch (IOException | java.awt.FontFormatException e) {
                throw new IllegalStateException("Failed to load " + FONT_RESOURCE, e);
            }
        });
    }
}
