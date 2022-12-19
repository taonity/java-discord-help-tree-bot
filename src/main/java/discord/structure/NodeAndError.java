package discord.structure;

import discord.tree.Node;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
public class NodeAndError {
    private final Node node;
    private final String errorMessage;
}
