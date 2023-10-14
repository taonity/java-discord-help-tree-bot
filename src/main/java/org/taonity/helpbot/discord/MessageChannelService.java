package org.taonity.helpbot.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;

@Component
@RequiredArgsConstructor
public class MessageChannelService {
    private final GuildSettingsRepository guildSettingsRepository;

    private MessageChannel getChannelByChannelId(Guild guild, String channelId) {
        final var snowflakeChannelId = Snowflake.of(channelId);
        return (MessageChannel) guild.getChannelById(snowflakeChannelId)
                .blockOptional()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20057));
    }

    private MessageChannel getChannelByGuild(
            Guild guild,
            Function<GuildSettings, String> channelSupplier,
            LogMessage onEmptyGuildSettings,
            LogMessage onEmptyGuild) {
        return Optional.ofNullable(guild)
                .map(Guild::getId)
                .map(Snowflake::asString)
                .map(guildSettingsRepository::findGuildSettingByGuildId)
                .orElseThrow(() -> new EmptyOptionalException(onEmptyGuildSettings))
                .map(channelSupplier)
                .map(channelId -> getChannelByChannelId(guild, channelId))
                .orElseThrow(() -> new EmptyOptionalException(onEmptyGuild));
    }

    public MessageChannel getChannel(Guild guild, ChannelRole channelRole) {
        switch (channelRole) {
            case HELP:
                return getChannelByGuild(
                        guild, GuildSettings::getHelpChannelId, LogMessage.ALERT_20057, LogMessage.ALERT_20061);
            case LOG:
                return getChannelByGuild(
                        guild, GuildSettings::getLogChannelId, LogMessage.ALERT_20058, LogMessage.ALERT_20078);
            default:
                return null;
        }
    }

    @Transactional
    public void updateChannelById(String guildId, ChannelRole channelRole, String channelId) {
        switch (channelRole) {
            case HELP:
                guildSettingsRepository.updateHelpChannelId(guildId, channelId);
                break;
            case LOG:
                guildSettingsRepository.updateLogChannelId(guildId, channelId);
                break;
        }
    }
}
