package org.inn.lockbox.commands;

import org.inn.lockbox.services.LockboxSentinel;
import org.jline.terminal.Terminal;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.jline.tui.component.StringInput;
import org.springframework.shell.jline.tui.component.StringInput.StringInputContext;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccessCommands {
    
    private final LockboxSentinel sentinel;
    private final Terminal terminal;
    private final ResourceLoader resourceLoader;

    @Command(name = "getin", description = "Unlock the lockbox and access its contents")
    public String getIn() {
        if (sentinel.isUnlocked()) {
            return "You are already inside.";
        }

        StringInput component = new StringInput(terminal, "Master Passphrase: ", "");
        component.setResourceLoader(resourceLoader);
        component.setMaskCharacter('*');
         
        StringInputContext context = component.run(StringInputContext.empty());
        String password = context.getResultValue();

        try {
            if (password == null || password.isEmpty()) return "Aborted.";
            sentinel.permitEntry(password);
            return "Access Granted.";
        } catch (Exception e) {
            return "Access Denied.";
        }
    }
}
