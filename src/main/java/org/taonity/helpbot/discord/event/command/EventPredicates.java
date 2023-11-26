package org.taonity.helpbot.discord.event.command;

import static java.util.Objects.isNull;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Entity;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.PartialMember;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.event.joinleave.service.GuildRoleService;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class EventPredicates {

    private final GuildSettingsRepository guildSettingsRepository;

    private Mono<Boolean> filterByChannelRole(
            Mono<MessageChannel> messageChannelMono, Mono<Guild> guildMono, ChannelRole channelRole) {
        final var messageChannelIdMono = messageChannelMono.map(Entity::getId).map(Snowflake::asString);
        final var guildSettingRoleChannelIdMono = guildMono
                .map(Guild::getId)
                .map(Snowflake::asString)
                .flatMap(guildSettingsRepository::findGuildSettingByGuildId)
                .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20042)))
                .map(guildSettings -> guildSettings.getChannelId(channelRole));

        return Flux.zip(messageChannelIdMono, guildSettingRoleChannelIdMono, String::equals)
                .single();
    }

    public Mono<Boolean> filterByChannelRole(InteractionCreateEvent event, ChannelRole channelRole) {
        return filterByChannelRole(
                event.getInteraction().getChannel(), event.getInteraction().getGuild(), channelRole);
    }

    public Mono<Boolean> filterByChannelRole(MessageCreateEvent event, ChannelRole channelRole) {
        return filterByChannelRole(
                event.getMessage().getChannel(), event.getMessage().getGuild(), channelRole);
    }

    public Mono<Boolean> filterEmptyAuthor(MessageCreateEvent event) {
        return Mono.just(event.getMessage().getAuthor().isPresent());
    }

    public Mono<Boolean> filterBot(MessageCreateEvent event) {
        final var messageAuthor =
                event.getMessage().getAuthor().orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20003));

        return Mono.just(!messageAuthor.isBot());
    }

    public Mono<Boolean> filterBot(InteractionCreateEvent event) {
        final var messageAuthor = event.getInteraction()
                .getMember()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20003));

        return Mono.just(!messageAuthor.isBot());
    }

    private Mono<Boolean> filterIfChannelExistsInSettings(Mono<Guild> guildMono, ChannelRole channelRole) {
        return guildMono
                .map(Guild::getId)
                .map(Snowflake::asString)
                .flatMap(guildSettingsRepository::findGuildSettingByGuildId)
                .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20056)))
                .map(guildSettings -> !isNull(guildSettings.getChannelId(channelRole)));
    }

    public Mono<Boolean> filterIfChannelsExistInSettings(InteractionCreateEvent event) {
        return Flux.zip(
                        filterIfChannelExistsInSettings(event, ChannelRole.HELP),
                        filterIfChannelExistsInSettings(event, ChannelRole.LOG),
                        (helpChannelExists, logChannelExists) -> helpChannelExists && logChannelExists)
                .single();
    }

    public Mono<Boolean> filterIfChannelExistsInSettings(InteractionCreateEvent event, ChannelRole channelRole) {
        return filterIfChannelExistsInSettings(event.getInteraction().getGuild(), channelRole);
    }

    public Mono<Boolean> filterIfIsGuildChannel(MessageCreateEvent event) {
        return event.getMessage().getGuild().flatMap(guild -> Mono.fromCallable(() -> !isNull(guild)));
    }

    public Mono<Boolean> filterIfChannelExistsInSettings(MessageCreateEvent event, ChannelRole channelRole) {
        return filterIfChannelExistsInSettings(event.getMessage().getGuild(), channelRole);
    }

    public Mono<Boolean> filterByModeratorRole(InteractionCreateEvent event) {
        return event.getInteraction()
                .getMember()
                .map(PartialMember::getRoles)
                // TODO: Why doesn't works?
                // .map(roles -> roles.collectSortedList(role -> role.getName().equals(GuildRoleService.ROLE_NAME)))
                .map(Flux::collectList)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20065))
                .map(roles ->
                        roles.stream().map(Role::getName).toList().contains(GuildRoleService.MODERATOR_ROLE_NAME));
    }
}
