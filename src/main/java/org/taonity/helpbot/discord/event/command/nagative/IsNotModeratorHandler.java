package org.taonity.helpbot.discord.event.command.nagative;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.CommandName;
import org.taonity.helpbot.discord.embed.EmbedBuilder;
import org.taonity.helpbot.discord.embed.EmbedType;
import org.taonity.helpbot.discord.event.command.AbstractSlashCommand;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.event.joinleave.service.GuildRoleService;
import org.taonity.helpbot.discord.localisation.SimpleMessage;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.client.ModeratorRoleNotFoundException;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class IsNotModeratorHandler extends AbstractSlashCommand {
    @Getter
    private final CommandName command = CommandName.CONFIG;

    private final EventPredicates eventPredicates;

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                        .filter(eventPredicates::filterBot)
                        .filter(this::filterByCommand)
                        .filter(e -> !eventPredicates.filterByModeratorRole(e))
                        .count()
                == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {
        final var guildId = event.getInteraction()
                .getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20071));

        final var messageString = event.getInteraction()
                .getGuild()
                .blockOptional()
                .map(Guild::getRoles)
                .map(Flux::collectList)
                .map(Mono::blockOptional)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20067))
                .map(Collection::stream)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20068))
                .filter(role -> role.getName().equals(GuildRoleService.MODERATOR_ROLE_NAME))
                .findFirst()
                .map(Role::getMention)
                .map(mention -> String.format(SimpleMessage.MUST_BE_MODERATOR_MESSAGE_FORMAT.getMessage(), mention))
                .orElseThrow(() -> new ModeratorRoleNotFoundException(LogMessage.ALERT_20059, guildId));

        event.reply()
                .withEmbeds(EmbedBuilder.buildSimpleMessage(messageString, EmbedType.SIMPLE_MESSAGE_EMBED_TYPE))
                .withEphemeral(true)
                .subscribe();

        log.info(
                "Command {} failed to execute by non-moderator user {} in guild {}",
                command.getCommandName(),
                event.getInteraction()
                        .getMember()
                        .map(Member::getId)
                        .map(Snowflake::asString)
                        .orElse("NULL"),
                event.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL"));
    }
}