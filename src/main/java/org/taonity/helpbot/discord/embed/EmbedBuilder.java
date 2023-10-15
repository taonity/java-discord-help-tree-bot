package org.taonity.helpbot.discord.embed;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.time.Instant;
import org.taonity.helpbot.discord.localisation.LocalizedMessage;
import org.taonity.helpbot.discord.localisation.SimpleMessage;

public class EmbedBuilder {
    public static final String LOG_ATTACHMENT_FILE_NAME = "file-name.txt";

    public static EmbedCreateSpec buildTipEmbed(String description) {
        return EmbedCreateSpec.builder()
                .color(Color.CYAN)
                .title(LocalizedMessage.TIP_MESSAGE.getMerged())
                .description(description)
                .footer(SimpleMessage.TIP_FOOTER_MESSAGE.getMessage(), "")
                .build();
    }

    public static EmbedCreateSpec buildMessageEmbed(String description, EmbedType embedType) {
        return EmbedCreateSpec.builder()
                .color(embedType.getColor())
                .title(embedType.getSimpleMessage().getMessage())
                .description(description)
                .timestamp(Instant.now())
                .footer(SimpleMessage.LOG_FOOTER_MESSAGE.getMessage(), "")
                .build();
    }

    public static EmbedCreateSpec buildSimpleMessage(String description, EmbedType embedType) {
        return EmbedCreateSpec.builder()
                .color(embedType.getColor())
                .description(description)
                .build();
    }
}
