package discord.handler.command;

import static discord.structure.CommandName.DIALOG;

import discord.exception.client.ModeratorRoleNotFoundException;
import discord.exception.main.EmptyOptionalException;
import discord.handler.EventPredicates;
import discord.localisation.SimpleMessage;
import discord.logging.LogMessage;
import discord.services.GuildRoleService;
import discord.services.MessageChannelService;
import discord.structure.CommandName;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class IsNotModeratorHandler extends AbstractSlashCommand {
    @Getter
    private final CommandName command = DIALOG;

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
                .filter(role -> role.getName().equals(GuildRoleService.ROLE_NAME))
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
