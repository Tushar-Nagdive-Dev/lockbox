package org.inn.lockbox.components;

import lombok.Builder;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.regex.Pattern;

@Builder
public class LockboxInput {

    private final Terminal terminal;
    private final String label;
    private final boolean required;
    private final Character mask;
    private final String pattern;
    private final Integer min;
    private final Integer max;
    private final String validationErrorMessage;

    public String run() {
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .highlighter(createCustomHighlighter())
                .build();

        while (true) {
            String prompt = new AttributedStringBuilder()
                    .style(AttributedStyle.DEFAULT.bold().foreground(AttributedStyle.BLUE))
                    .append(label)
                    .style(AttributedStyle.DEFAULT)
                    .append(required ? " * " : " ")
                    .append("➔ ")
                    .toAnsi();

            // Read the input
            String result = reader.readLine(prompt, mask);

            // Clean the right-side stats after Enter is pressed
            clearInlineStats();

            String error = getValidationError(result);
            if (error == null) return result;

            // Show error message
            terminal.writer().println("\u001B[31m  ✘ " + error + "\u001B[0m");
            terminal.flush();
        }
    }

    private Highlighter createCustomHighlighter() {
        return new Highlighter() {
            @Override
            public AttributedString highlight(LineReader reader, String buffer) {
                return doHighlight(buffer);
            }

            public AttributedString highlight(LineReader reader, org.jline.reader.Buffer buffer) {
                return doHighlight(buffer.toString());
            }

            @Override public void setErrorIndex(int index) {}
            @Override public void setErrorPattern(Pattern pattern) {}

            private AttributedString doHighlight(String currentText) {
                // UPDATE: Inline stats on the same line to prevent "staircase"
                renderInlineStats(currentText);

                boolean isValid = getValidationError(currentText) == null;
                AttributedStringBuilder asb = new AttributedStringBuilder();

                asb.style(AttributedStyle.DEFAULT.foreground(isValid ? AttributedStyle.GREEN : AttributedStyle.RED));

                if (mask != null && currentText.length() > 0) {
                    asb.append(String.valueOf(mask).repeat(currentText.length()));
                } else {
                    asb.append(currentText);
                }

                return asb.toAttributedString();
            }
        };
    }

    private void renderInlineStats(String currentText) {
        int chars = currentText.length();
        String maxStr = (max != null) ? String.valueOf(max) : "∞";

        // ANSI SEQUENCE EXPLANATION:
        // \u001B[s  -> Save cursor position
        // \u001B[60G -> Move cursor to column 60 (Adjust 60 if your terminal is narrow)
        // \u001B[K   -> Clear from cursor to end of line (removes old stats)
        // \u001B[u   -> Restore cursor to typing position

        terminal.writer().print("\u001B[s");
        terminal.writer().print("\u001B[60G");
        terminal.writer().print("\u001B[K");
        terminal.writer().print(String.format("\u001B[2m(Chars: %d/%s)\u001B[0m", chars, maxStr));
        terminal.writer().print("\u001B[u");
        terminal.flush();
    }

    private void clearInlineStats() {
        terminal.writer().print("\u001B[s");
        terminal.writer().print("\u001B[60G");
        terminal.writer().print("\u001B[K");
        terminal.writer().print("\u001B[u");
        terminal.flush();
    }

    private String getValidationError(String input) {
        if (input == null) return "Input error.";
        if (required && input.trim().isEmpty()) return "Required field.";
        if (min != null && input.length() < min) return "Min length: " + min;
        if (max != null && input.length() > max) return "Max length: " + max;
        if (pattern != null && !Pattern.matches(pattern, input)) {
            return (validationErrorMessage != null) ? validationErrorMessage : "Invalid format.";
        }
        return null;
    }
}