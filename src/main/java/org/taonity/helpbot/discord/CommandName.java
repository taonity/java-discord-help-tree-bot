package org.taonity.helpbot.discord;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CommandName {
    QUESTION("question"),
    CHANNELROLE("channelrole"),
    CONFIG("config"),
    ANY("");

    @Getter
    private final String commandName;
}