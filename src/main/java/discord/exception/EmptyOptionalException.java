package discord.exception;

import discord.localisation.LogMessage;
import discord.services.MessageChannelService;
import discord.structure.ErrorEmbedType;

public class EmptyOptionalException extends DiscordLoggingException {
    public EmptyOptionalException(LogMessage logMessage, MessageChannelService messageChannelService) {
        super(logMessage, messageChannelService, ErrorEmbedType.EXPECTED_EMBED_TYPE);
    }
}
