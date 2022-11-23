package discord.exception;

import discord.localisation.LogMessage;
import discord.services.MessageChannelService;
import discord.structure.ErrorEmbedType;

public class NullObjectException extends DiscordLoggingException {
    public NullObjectException(LogMessage logMessage, MessageChannelService messageChannelService) {
        super(logMessage, messageChannelService, ErrorEmbedType.EXPECTED_EMBED_TYPE);
    }
}
