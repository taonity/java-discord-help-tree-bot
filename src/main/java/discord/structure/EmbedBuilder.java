package discord.structure;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.time.Instant;

import static discord.localisation.LocalizedMessage.TIP_MESSAGE;
import static discord.localisation.SimpleMessage.*;

public class EmbedBuilder {
    public static final String LOG_ATTACHMENT_FILE_NAME = "file-name.txt";
    public static final String LOG_ATTACHMENT_FILE_PATH = String.format("attachment://%s", LOG_ATTACHMENT_FILE_NAME);

    public static EmbedCreateSpec buildTipEmbed(String description) {
        return EmbedCreateSpec.builder()
                .color(Color.CYAN)
                .title(TIP_MESSAGE.getMerged())
                .description(description)
                .footer(TIP_FOOTER_MESSAGE.getMessage(), "")
                .build();
    }

    public static EmbedCreateSpec buildMessageEmbed(String description, EmbedType embedType) {
        return EmbedCreateSpec.builder()
                .color(embedType.getColor())
                .title(embedType.getSimpleMessage().getMessage())
                .description(description)
                .timestamp(Instant.now())
                .footer(LOG_FOOTER_MESSAGE.getMessage(), "")
                .build();
    }

    public static EmbedCreateSpec buildSimpleMessage(String description, EmbedType embedType) {
        return EmbedCreateSpec.builder()
                .color(embedType.getColor())
                .description(description)
                .build();
    }

}
