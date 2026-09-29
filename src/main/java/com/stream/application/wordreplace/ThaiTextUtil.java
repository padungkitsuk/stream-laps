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
    // Regular space used to pad justified lines. Not U+2009: the font has no glyph for it, so a lone one measures ~9.7pt but renders ~2.8pt, and lines came out short. Regular space measures the same everywhere, and can be repeated to pad a
    // line out to a target width for justification.
    private static final String PAD_SPACE = " ";
    // A wrapped line that would need more than this many pad spaces per gap is left unjustified: it is a short line
    // ended early by an unbreakable long token on the next line, and stretching it looks like odd wide gaps.
    private static final int MAX_PAD_SPACES_PER_GAP = 3;
    // addThaiWordBreakJustifiedParagraph is for table cells that must be justified edge to edge, so it allows wider gaps.
    private static final int PARAGRAPH_MAX_PAD_SPACES_PER_GAP = 8;
    // A segment wider than this share of the line is a long token (URL, code, text typed without spaces): it is broken
    // between characters to fill the rest of a line, instead of leaving a short line in front of it or overflowing.
    private static final float LONG_TOKEN_SHARE = 1f / 3f;
    // ...but only when at least this share of the line is left for it; otherwise it starts on the next line.
    private static final float MIN_ROOM_TO_BREAK_SHARE = 0.1f;
    // Where a long token is broken, a point right after one of these is preferred (www.site.com. | com), as long as
    // it gives up no more than MAX_PUNCTUATION_LOSS_SHARE of the line.
    private static final String BREAK_AFTER = "./-_,;:?&=#@";
    private static final float MAX_PUNCTUATION_LOSS_SHARE = 0.15f;
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
     * Wraps each paragraph at ICU word boundaries into lines of at most {@code widthPoints}, joined by explicit newlines (JasperReports does not treat ZWSP as a break opportunity, so it must not be left to re-wrap). Every
     * wrapped line (except the last line of a paragraph, and any line with only one segment)
     * is right-justified to {@code widthPoints} by padding the gaps between ICU word-break
     * segments with spaces, sized using the real glyph metrics of {@code fontSizePt} pt
     * TH Sarabun. Thai has no natural inter-word space to stretch (unlike Latin text), so this
     * measures and pads manually instead of relying on JasperReports' own Justified alignment,
     * which has no effect on text built from {@link #addThaiWordBreakPreserveNewLine(String)}.
     * Lines without Thai are padded only at their spaces. A token wider than a third of the line
     * (URL, code, text typed without spaces) is broken between characters to fill the line.
     *
     * @param widthPoints the text field's usable width in points (box width minus padding/indent)
     */
    public static String addThaiWordBreakJustified(String input, float widthPoints, float fontSizePt) {
        return wrapText(input, widthPoints, fontSizePt, MAX_PAD_SPACES_PER_GAP);
    }

    /**
     * Same as {@link #addThaiWordBreakJustified(String, float, float)}, but the whole text is one paragraph: every
     * line break (with the spaces around it) becomes a single space, so text typed or pasted with hard line breaks
     * flows on, and every line but the last is justified even when that needs wider gaps, for free text shown in a
     * table cell.
     */
    public static String addThaiWordBreakJustifiedParagraph(String input, float widthPoints, float fontSizePt) {
        return wrapText(input.replaceAll("\\s*\\R\\s*", " ").strip(), widthPoints, fontSizePt, PARAGRAPH_MAX_PAD_SPACES_PER_GAP);
    }

    /**
     * Same wrapping as {@link #addThaiWordBreakJustified(String, float, float)} (ICU word boundaries, explicit
     * newlines, lines of at most {@code widthPoints}) but the lines are not padded, for text where extra spaces
     * would be wrong: names, addresses, URLs, e-mail.
     */
    public static String addThaiWordBreakWrapped(String input, float widthPoints, float fontSizePt) {
        return wrapText(input, widthPoints, fontSizePt, 0);
    }

    // maxPadSpacesPerGap 0 means lines are not justified
    private static String wrapText(String input, float widthPoints, float fontSizePt, int maxPadSpacesPerGap) {
        Font font = font(fontSizePt);
        FontRenderContext frc = new FontRenderContext(null, true, true);

        String[] lines = input.split("\\r?\\n");
        StringBuilder finalResult = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            finalResult.append(justifyLine(lines[i], widthPoints, font, frc, maxPadSpacesPerGap));

            if (i < lines.length - 1) {
                finalResult.append("\n");
            }
        }

        return finalResult.toString();
    }

    private static String justifyLine(String line, float widthPoints, Font font, FontRenderContext frc, int maxPadSpacesPerGap) {
        List<String> segments = segment(line);
        List<List<String>> wrapped = wrap(segments, widthPoints, font, frc);

        StringBuilder out = new StringBuilder();
        for (int li = 0; li < wrapped.size(); li++) {
            List<String> subLine = wrapped.get(li);
            // spaces at the end of a line are invisible, and wrap() let them hang past the edge
            int last = subLine.size() - 1;
            subLine.set(last, stripTrailingWhitespace(subLine.get(last)));
            if (subLine.get(last).isEmpty() && last > 0) {
                subLine.remove(last);
            }
            boolean lastSubLine = (li == wrapped.size() - 1);
            out.append(li > 0 ? "\n" : "").append(maxPadSpacesPerGap == 0 || lastSubLine || subLine.size() < 2
                    ? joinUnjustified(subLine)
                    : joinJustified(subLine, widthPoints, maxPadSpacesPerGap, font, frc));
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
            String rest = seg;
            while (!rest.isEmpty()) {
                float fitWidth = width(stripTrailingWhitespace(rest), font, frc);
                if (currentWidth + fitWidth <= widthPoints) {
                    current.add(rest);
                    currentWidth += width(rest, font, frc);
                    break;
                }
                float room = widthPoints - currentWidth;
                if (fitWidth > widthPoints * LONG_TOKEN_SHARE && (current.isEmpty() || room >= widthPoints * MIN_ROOM_TO_BREAK_SHARE)) {
                    int cut = fittingPrefixLength(rest, room, widthPoints * MAX_PUNCTUATION_LOSS_SHARE, font, frc);
                    if (cut == 0 && current.isEmpty()) {
                        cut = firstCharacterLength(rest);
                    }
                    current.add(rest.substring(0, cut));
                    rest = rest.substring(cut);
                } else if (current.isEmpty()) {
                    // only with a width too small for any text: place it anyway so the loop always moves on
                    current.add(rest);
                    currentWidth += width(rest, font, frc);
                    break;
                }
                current.removeIf(String::isEmpty);
                if (!current.isEmpty()) {
                    wrapped.add(current);
                }
                current = new ArrayList<>();
                currentWidth = 0f;
            }
        }
        if (!current.isEmpty()) {
            wrapped.add(current);
        }
        return wrapped;
    }

    // Length of the longest prefix of text, ending on a character (grapheme) boundary, that fits in room; 0 if none does.
    // Never ends right after a Thai leading vowel (เ แ โ ใ ไ), which belongs with the consonant after it, and ends
    // after punctuation instead when that is at most maxLoss shorter.
    private static int fittingPrefixLength(String text, float room, float maxLoss, Font font, FontRenderContext frc) {
        BreakIterator chars = BreakIterator.getCharacterInstance(new Locale("th", "TH"));
        chars.setText(text);
        List<Integer> ends = new ArrayList<>();
        for (int end = chars.next(); end != BreakIterator.DONE; end = chars.next()) {
            ends.add(end);
        }
        int best = -1;
        int lo = 0;
        int hi = ends.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (width(text.substring(0, ends.get(mid)), font, frc) <= room) {
                best = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        while (best >= 0 && isThaiLeadingVowel(text.charAt(ends.get(best) - 1))) {
            best--;
        }
        for (int i = best; i >= 0; i--) {
            String prefix = text.substring(0, ends.get(i));
            if (width(prefix, font, frc) < room - maxLoss) {
                break;
            }
            if (BREAK_AFTER.indexOf(prefix.charAt(prefix.length() - 1)) >= 0) {
                return ends.get(i);
            }
        }
        return best >= 0 ? ends.get(best) : 0;
    }

    private static int firstCharacterLength(String text) {
        BreakIterator chars = BreakIterator.getCharacterInstance(new Locale("th", "TH"));
        chars.setText(text);
        return chars.next();
    }

    private static boolean isThaiLeadingVowel(char c) {
        return c >= 'เ' && c <= 'ไ';
    }

    private static boolean isThai(String text) {
        return text.chars().anyMatch(c -> c >= 0x0E00 && c <= 0x0E7F);
    }

    private static boolean endsWithWhitespace(String text) {
        return !text.isEmpty() && Character.isWhitespace(text.charAt(text.length() - 1));
    }

    private static String stripTrailingWhitespace(String text) {
        int end = text.length();
        while (end > 0 && Character.isWhitespace(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(0, end);
    }

    private static String joinUnjustified(List<String> segments) {
        StringBuilder out = new StringBuilder();
        for (String seg : segments) {
            out.append(seg);
        }
        return out.toString();
    }

    private static String joinJustified(List<String> segments, float widthPoints, int maxPadSpacesPerGap, Font font, FontRenderContext frc) {
        float rawWidth = 0f;
        for (String seg : segments) {
            rawWidth += width(seg, font, frc);
        }

        // Latin text is stretched only at its spaces (not after a hyphen or slash); Thai rarely has spaces, so every
        // word boundary is used there.
        boolean[] padAfter = new boolean[segments.size() - 1];
        boolean spacesOnly = !isThai(String.join("", segments))
                && segments.subList(0, padAfter.length).stream().anyMatch(ThaiTextUtil::endsWithWhitespace);
        int gaps = 0;
        for (int i = 0; i < padAfter.length; i++) {
            padAfter[i] = !spacesOnly || endsWithWhitespace(segments.get(i));
            if (padAfter[i]) {
                gaps++;
            }
        }

        float slack = Math.max(0f, widthPoints - rawWidth - 0.5f);
        float padSpaceWidth = width(PAD_SPACE, font, frc);
        int totalPadSpaces = padSpaceWidth > 0f ? (int) Math.floor(slack / padSpaceWidth) : 0;
        if (totalPadSpaces > maxPadSpacesPerGap * gaps) {
            return joinUnjustified(segments);
        }

        // Spread totalPadSpaces evenly across the gaps using running cumulative targets
        // (like Bresenham line drawing), instead of rounding slack/gaps per gap independently -
        // that loses the whole budget to underflow whenever the per-gap share is smaller than
        // one pad space, which is the common case once a line already nearly fills the width.
        StringBuilder out = new StringBuilder();
        int gapIndex = 0;
        int distributed = 0;
        for (int i = 0; i < segments.size(); i++) {
            out.append(segments.get(i));
            if (i < padAfter.length && padAfter[i]) {
                int cumulativeTarget = ++gapIndex * totalPadSpaces / gaps;
                int count = cumulativeTarget - distributed;
                distributed = cumulativeTarget;
                for (int t = 0; t < count; t++) {
                    out.append(PAD_SPACE);
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
