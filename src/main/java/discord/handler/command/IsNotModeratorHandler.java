package discord.handler.command;

import discord.exception.EmptyOptionalException;
import discord.handler.EventPredicates;
import discord.localisation.LogMessage;
import discord.localisation.SimpleMessage;
import discord.services.GuildRoleService;
import discord.services.MessageChannelService;
import discord.services.SelectMenuService;
import discord.structure.ChannelRole;
import discord.structure.CommandName;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.stream.Stream;

import static discord.localisation.LocalizedMessage.CHOOSE_LANGUAGE_MESSAGE;
import static discord.structure.CommandName.DIALOG;

@Component
@RequiredArgsConstructor
public class IsNotModeratorHandler extends AbstractSlashCommand {
    @Getter
    private final CommandName command = DIALOG;

    private final MessageChannelService messageChannelService;
    private final EventPredicates eventPredicates;

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                .filter(eventPredicates::filterBot)
                .filter(this::filterByCommand)
                .filter(e -> !eventPredicates.filterByModeratorRole(e))
                .count() == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {

        final var messageString = event.getInteraction().getGuild().blockOptional()
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
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20059));


        event.reply().withEmbeds(EmbedBuilder.buildSimpleMessage(
                messageString,
                EmbedType.SIMPLE_MESSAGE_EMBED_TYPE
        )).subscribe();

    }
}
