package org.taonity.helpbot.discord.embed;

import discord4j.rest.util.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.taonity.helpbot.discord.localisation.SimpleMessage;

@Getter
@AllArgsConstructor
public enum EmbedType {
    EXPECTED_EMBED_TYPE(SimpleMessage.EXPECTED_ERROR_MESSAGE, Color.ORANGE),
    UNEXPECTED_EMBED_TYPE(SimpleMessage.UNEXPECTED_ERROR_MESSAGE, Color.RED),
    INIT_EMBED_TYPE(SimpleMessage.INIT_ERROR_MESSAGE, Color.GRAY),
    WRONG_DIALOG_EMBED_TYPE(SimpleMessage.FAIL_DIALOG_UPDATE_MESSAGE, Color.YELLOW),
    SUCCESS_DIALOG_EMBED_TYPE(SimpleMessage.SUCCESS_DIALOG_UPDATE_MESSAGE, Color.GREEN),
    SIMPLE_MESSAGE_EMBED_TYPE(null, Color.CYAN);

    private final SimpleMessage simpleMessage;
    private final Color color;
}
