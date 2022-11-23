package discord.structure;

import discord.localisation.LogMessage;
import discord.localisation.SimpleMessage;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.time.Instant;

import static discord.localisation.LocalizedMessage.TIP_MESSAGE;
import static discord.localisation.SimpleMessage.*;

public class EmbedBuilder {
    public static final String LOG_ATTACHMENT_FILE_NAME = "file-name.png";
    public static final String LOG_ATTACHMENT_FILE_PATH = String.format("attachment://%s", LOG_ATTACHMENT_FILE_NAME);

    public static EmbedCreateSpec buildTipEmbed(String description) {
        return EmbedCreateSpec.builder()
                .color(Color.CYAN)
                .title(TIP_MESSAGE.getMerged())
                .description(description)
                .footer(TIP_FOOTER_MESSAGE.getMessage(), "")
                .build();
    }

    public static EmbedCreateSpec buildLogEmbed(LogMessage logMessage, ErrorEmbedType errorEmbedType) {
        return EmbedCreateSpec.builder()
                .color(errorEmbedType.getColor())
                .title(errorEmbedType.getSimpleMessage().getMessage())
                .description(logMessage.name())
                .image(LOG_ATTACHMENT_FILE_PATH)
                .timestamp(Instant.now())
                .footer(LOG_FOOTER_MESSAGE.getMessage(), "")
                .build();
    }
}
