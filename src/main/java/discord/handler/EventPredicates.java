package discord.handler;

import discord.exception.EmptyOptionalException;
import discord.localisation.LogMessage;
import discord.repository.GuildSettingsRepository;
import discord.services.GuildRoleService;
import discord.structure.ChannelRole;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventPredicates {

    private final GuildSettingsRepository guildSettingsRepository;

    private boolean filterByChannelRole(Mono<MessageChannel> messageChannelMono, Mono<Guild> guildMono, ChannelRole channelRole) {
        final var currentChannelId = messageChannelMono.blockOptional()
                .map(Entity::getId)
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20007));

        return guildMono.blockOptional()
                .map(Guild::getId)
                .map(Snowflake::asString)
                .map(guildSettingsRepository::findGuildSettingByGuildId)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20042))
                .map(guildSettings -> guildSettings.getChannelId(channelRole))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20044))
                .equals(currentChannelId);
    }

    public boolean filterByChannelRole(InteractionCreateEvent event, ChannelRole channelRole) {
        return filterByChannelRole(event.getInteraction().getChannel(), event.getInteraction().getGuild(), channelRole);
    }


    public boolean filterByChannelRole(MessageCreateEvent event, ChannelRole channelRole) {
        return filterByChannelRole(event.getMessage().getChannel(), event.getMessage().getGuild(), channelRole);
    }

    public boolean filterByAuthorId(InteractionCreateEvent event, List<String> userWhiteList) {
        var member = event.getInteraction().getMember()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20009));

        final String authorId = member.getId().asString();
        return userWhiteList.contains(authorId);
    }

    public boolean filterEmptyAuthor(MessageCreateEvent event) {
        return event.getMessage().getAuthor().isPresent();
    }

    public boolean filterBot(MessageCreateEvent event) {
        final var messageAuthor = event.getMessage().getAuthor()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20003));

        return !messageAuthor.isBot();
    }

    public boolean filterBot(InteractionCreateEvent event) {
        final var messageAuthor = event.getInteraction().getMember()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20003));

        return !messageAuthor.isBot();
    }

    private boolean filterIfChannelExistsInSettings(Mono<Guild> guildMono, ChannelRole channelRole) {
        return guildMono.blockOptional()
                .map(Guild::getId)
                .map(Snowflake::asString)
                .map(guildSettingsRepository::findGuildSettingByGuildId)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20056))
                .map(guildSettings -> guildSettings.getChannelId(channelRole))
                .isPresent();
    }

    public boolean filterIfChannelsExistInSettings(InteractionCreateEvent event) {
        final var helpChannelExists = filterIfChannelExistsInSettings(event, ChannelRole.HELP);
        final var logChannelExists = filterIfChannelExistsInSettings(event, ChannelRole.LOG);
        return helpChannelExists && logChannelExists;
    }

    public boolean filterIfChannelExistsInSettings(InteractionCreateEvent event, ChannelRole channelRole) {
        return filterIfChannelExistsInSettings(event.getInteraction().getGuild(), channelRole);
    }

    public boolean filterIfChannelExistsInSettings(MessageCreateEvent event, ChannelRole channelRole) {
        return filterIfChannelExistsInSettings(event.getMessage().getGuild(), channelRole);
    }

    public boolean filterByModeratorRole(InteractionCreateEvent event) {
        return event.getInteraction().getMember()
                .map(PartialMember::getRoles)
                // TODO: Why doesn't works?
                //.map(roles -> roles.collectSortedList(role -> role.getName().equals(GuildRoleService.ROLE_NAME)))
                .map(Flux::collectList)
                .map(Mono::blockOptional)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20065))
                .map(Collection::stream)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20066))
                .map(Role::getName)
                .collect(Collectors.toList())
                .contains(GuildRoleService.ROLE_NAME);
    }
}
