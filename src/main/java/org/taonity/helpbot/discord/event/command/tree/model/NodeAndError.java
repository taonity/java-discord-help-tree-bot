package org.taonity.helpbot.discord.event.command.tree.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NodeAndError {
    private final Node node;
    private final String errorMessage;
}
