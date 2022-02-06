package discord.structure;

import discord.tree.Node;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NodeAndError {
    private final Node node;
    private final String errorMessage;
}
