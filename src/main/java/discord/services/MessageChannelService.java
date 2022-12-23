package discord.services;

import discord.exception.EmptyOptionalException;
import discord.exception.NullObjectException;
import discord.localisation.LogMessage;
import discord.structure.ChannelRole;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class MessageChannelService {
    private final GuildSettingsRepository guildSettingsRepository;

    private MessageChannel getChannelByChannelId(Guild guild, String channelId) {
        final var snowflakeChannelId = Snowflake.of(channelId);
        final var messageChannel = (MessageChannel) guild.getChannelById(snowflakeChannelId).block();
        if(isNull(messageChannel)) {
            throw new NullObjectException(LogMessage.ALERT_20057);
        }
        return (MessageChannel) guild.getChannelById(snowflakeChannelId).block();
    }

    private MessageChannel getChannelByGuild(Guild guild, Function<GuildSettings, String> channelSupplier, LogMessage logMessage) {
        if(isNull(guild)) {
            throw new NullObjectException(LogMessage.ALERT_20061);
        }
        return guildSettingsRepository
                .findGuildSettingByGuildId(guild.getId().asString())
                .map(channelSupplier)
                .map(channelId -> getChannelByChannelId(guild, channelId))
                .orElseThrow(() -> new EmptyOptionalException(logMessage));
    }

    public MessageChannel getChannel(Guild guild, ChannelRole channelRole) {
        switch (channelRole) {
            case HELP:
                return getChannelByGuild(guild, GuildSettings::getHelpChannelId, LogMessage.ALERT_20057);
            case LOG:
                return getChannelByGuild(guild, GuildSettings::getLogChannelId, LogMessage.ALERT_20058);
            default:
                return null;
        }
    }

    @Transactional
    public void updateChannelById(String guildId, ChannelRole channelRole, String channelId) {
        switch (channelRole) {
            case HELP:
                guildSettingsRepository.updateHelpChannelId(guildId, channelId);
            case LOG:
                guildSettingsRepository.updateLogChannelId(guildId, channelId);
        }
    }
}
