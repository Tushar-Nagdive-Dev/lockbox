package org.inn.lockbox.common;

public class ColourUtils {
    // Basic colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String BRIGHT_BLACK = "\u001B[90m";
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";
    public static final String ORANGE = "\u001B[38;5;208m";
    public static final String PURPLE = "\u001B[38;5;129m";
    public static final String ORANGE_RGB = "\u001B[38;2;255;165;0m";
    public static final String PURPLE_RGB = "\u001B[38;2;128;0;128m";
    public static final String GRAY_MEDIUM = "\u001B[38;5;244m";
    public static final String GRAY_DARK = "\u001B[38;5;240m";
    public static final String GRAY_RGB = "\u001B[38;2;128;128;128m";

    // Basic backgrounds
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_MAGENTA = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";
    public static final String BG_BRIGHT_BLACK = "\u001B[100m";
    public static final String BG_BRIGHT_RED = "\u001B[101m";
    public static final String BG_BRIGHT_GREEN = "\u001B[102m";
    public static final String BG_BRIGHT_YELLOW = "\u001B[103m";
    public static final String BG_BRIGHT_BLUE = "\u001B[104m";
    public static final String BG_BRIGHT_MAGENTA = "\u001B[105m";
    public static final String BG_BRIGHT_CYAN = "\u001B[106m";
    public static final String BG_BRIGHT_WHITE = "\u001B[107m";
    public static final String BG_ORANGE = "\u001B[48;5;208m";
    public static final String BG_PURPLE_RGB = "\u001B[48;2;128;0;128m";
    public static final String BG_GRAY = "\u001B[48;5;244m";

    // Text attributes
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String BLINK = "\u001B[5m";
    public static final String REVERSE = "\u001B[7m";
    public static final String HIDDEN = "\u001B[8m";
    public static final String STRIKETHROUGH = "\u001B[9m";

    // 256‑color helpers
    public static String fg256(int index) {
        return "\u001B[38;5;" + index + "m";
    }

    public static String bg256(int index) {
        return "\u001B[48;5;" + index + "m";
    }

    // TrueColor helpers
    public static String fgRgb(int r, int g, int b) {
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }

    public static String bgRgb(int r, int g, int b) {
        return "\u001B[48;2;" + r + ";" + g + ";" + b + "m";
    }

    // Convenience: combine foreground, background, and attributes
    public static String style(String fg, String bg, String... attrs) {
        StringBuilder sb = new StringBuilder("\u001B[");
        for (String attr : attrs) {
            // Extract numeric code from e.g. "\u001B[1m" -> "1"
            sb.append(attr.replaceAll("\u001B\\[", "").replace("m", "")).append(";");
        }
        sb.append(fg.replaceAll("\u001B\\[", "").replace("m", "")).append(";");
        sb.append(bg.replaceAll("\u001B\\[", "").replace("m", ""));
        sb.append("m");
        return sb.toString();
    }
}