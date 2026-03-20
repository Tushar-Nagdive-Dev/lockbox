package org.inn.lockbox.components;

import java.util.ArrayList;
import java.util.List;
import org.inn.lockbox.common.ColourUtils;

public class ListDataView {

    private final String title;
    private final List<String[]> entries = new ArrayList<>();

    public ListDataView(String title) {
        this.title = title;
    }

    public ListDataView add(String cmd, String desc, String example) {
        entries.add(new String[]{cmd, desc, example});
        return this;
    }

    public String render() {
        if(entries.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(ColourUtils.WHITE).append(" ● ").append(title).append(ColourUtils.RESET).append("\n");
        sb.append(ColourUtils.GRAY_MEDIUM).append(" ──────────────────────────────────────────────────────────────────────────").append(ColourUtils.RESET).append("\n");

        for(String[] entry: entries) {
            // Updated padding: %-20s for command, %-35s for description
            String row = String.format("  %s%-20s%s || %-35s || %s%s%s", 
                ColourUtils.CYAN, entry[0], ColourUtils.RESET, 
                entry[1], 
                ColourUtils.GRAY_MEDIUM, entry[2], ColourUtils.RESET);
            sb.append(row).append("\n");
        }

        entries.clear();
        return sb.toString();
    }
}