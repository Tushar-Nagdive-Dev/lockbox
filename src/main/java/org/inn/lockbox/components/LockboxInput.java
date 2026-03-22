package org.inn.lockbox.components;

import lombok.Builder;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

@Builder
public class LockboxInput {

    private final Terminal terminal;

    private final String label;

    private final String defaultValue;

    private final boolean required;

    private final Character mask;

    private final Integer min;

    private final Integer max;

    private final String pattern;

    private final String validationErrorMessage;

    public String run() {
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

        while (true) {
            String fullPrompt = renderPrompt();
            String input = reader.readLine(fullPrompt, mask);

            if(input == null || input.trim().isEmpty()) {
                input = (defaultValue != null) ? defaultValue : "";
            }

            String error = validate(input);
            if(error == null) {
                terminal.writer().print("\r\u001B[K");
                terminal.flush();
                return input;
            }

            terminal.writer().println("\u001B[31m>> "+error+"\u001B[0m");
            terminal.flush();
        }
    }

    private String renderPrompt() {
        AttributedStringBuilder asb = new AttributedStringBuilder();
        asb.append(label, AttributedStyle.DEFAULT.bold());
        if(required) asb.append(" *", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
        asb.append(": ");
        return asb.toAnsi();
    }

    private String validate(String input) {
        if(required && input.isEmpty()) return "This field is required";
        if(min != null && input.length() < min) return "Minimum length is " + min;
        if(max != null && input.length() > max) return "Maximum length is " + max;
        if(pattern != null && !input.matches(pattern)) {
            return (validationErrorMessage != null) ? validationErrorMessage : "invalid format.";
        }
        return null;
    }


}
