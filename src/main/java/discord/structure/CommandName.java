package discord.structure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommandName {
    QUESTION("question"),
    CHANNELROLE("channelrole"),
    DIALOG("dialog"),
    ANY("");

    @Getter
    private final String commandName;
}
