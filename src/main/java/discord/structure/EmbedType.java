package discord.structure;

import static discord.localisation.SimpleMessage.*;

import discord.localisation.SimpleMessage;
import discord4j.rest.util.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmbedType {
    EXPECTED_EMBED_TYPE(EXPECTED_ERROR_MESSAGE, Color.ORANGE),
    UNEXPECTED_EMBED_TYPE(UNEXPECTED_ERROR_MESSAGE, Color.RED),
    INIT_EMBED_TYPE(INIT_ERROR_MESSAGE, Color.GRAY),
    WRONG_DIALOG_EMBED_TYPE(FAIL_DIALOG_UPDATE_MESSAGE, Color.YELLOW),
    SUCCESS_DIALOG_EMBED_TYPE(SUCCESS_DIALOG_UPDATE_MESSAGE, Color.GREEN),
    SIMPLE_MESSAGE_EMBED_TYPE(null, Color.CYAN);

    private final SimpleMessage simpleMessage;
    private final Color color;
}
