package discord.structure;

import discord.localisation.SimpleMessage;
import discord4j.rest.util.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static discord.localisation.SimpleMessage.*;

@Getter
@AllArgsConstructor
public enum ErrorEmbedType {
    EXPECTED_EMBED_TYPE(EXPECTED_ERROR_MESSAGE, Color.YELLOW),
    UNEXPECTED_EMBED_TYPE(UNEXPECTED_ERROR_MESSAGE, Color.RED),
    INIT_EMBED_TYPE(INIT_ERROR_MESSAGE, Color.GRAY);

    private final SimpleMessage simpleMessage;
    private final Color color;
}
