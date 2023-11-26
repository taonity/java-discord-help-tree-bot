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
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MessageChannelService {
    private final GuildSettingsRepository guildSettingsRepository;

    private Mono<MessageChannel> getChannelByChannelId(Guild guild, String channelId) {
        return guild.getChannelById(Snowflake.of(channelId))
                .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20057)))
                .cast(MessageChannel.class);
    }

    private Mono<MessageChannel> getChannelByGuild(
            Guild guild,
            Function<GuildSettings, String> channelSupplier,
            LogMessage onEmptyGuildSettings,
            LogMessage onEmptyGuild) {
        return Optional.ofNullable(guild)
                .map(Guild::getId)
                .map(Snowflake::asString)
                .map(guildSettingsRepository::findGuildSettingByGuildId)
                .orElseThrow(() -> new EmptyOptionalException(onEmptyGuildSettings))
                .switchIfEmpty(Mono.error(new EmptyOptionalException(onEmptyGuild)))
                .map(channelSupplier)
                .flatMap(channelId -> getChannelByChannelId(guild, channelId));
    }

    public Mono<MessageChannel> getChannel(Guild guild, ChannelRole channelRole) {
        return switch (channelRole) {
            case HELP -> getChannelByGuild(
                    guild, GuildSettings::getHelpChannelId, LogMessage.ALERT_20057, LogMessage.ALERT_20061);
            case LOG -> getChannelByGuild(
                    guild, GuildSettings::getLogChannelId, LogMessage.ALERT_20058, LogMessage.ALERT_20078);
        };
    }

    @Transactional
    public Mono<Void> updateChannelById(String guildId, ChannelRole channelRole, String channelId) {
        return switch (channelRole) {
            case HELP -> guildSettingsRepository.updateHelpChannelId(guildId, channelId);
            case LOG -> guildSettingsRepository.updateLogChannelId(guildId, channelId);
        };
    }
}
