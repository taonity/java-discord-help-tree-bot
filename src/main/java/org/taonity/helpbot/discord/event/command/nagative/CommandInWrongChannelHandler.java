package org.taonity.helpbot.discord.event.command.nagative;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.CommandName;
import org.taonity.helpbot.discord.MessageChannelService;
import org.taonity.helpbot.discord.embed.EmbedBuilder;
import org.taonity.helpbot.discord.embed.EmbedType;
import org.taonity.helpbot.discord.event.command.AbstractSlashCommand;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.localisation.SimpleMessage;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandInWrongChannelHandler extends AbstractSlashCommand {

    @Getter
    private final CommandName command = CommandName.QUESTION;

    private final EventPredicates eventPredicates;
    private final MessageChannelService messageChannelService;

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                        .filter(this::filterByCommand)
                        .filter(eventPredicates::filterBot)
                        .filter(e -> eventPredicates.filterIfChannelExistsInSettings(e, ChannelRole.HELP))
                        .filter(e -> !eventPredicates.filterByChannelRole(e, ChannelRole.HELP))
                        .count()
                == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {

        final var messageString = event.getInteraction()
                .getGuild()
                .blockOptional()
                .map(guild -> messageChannelService.getChannel(guild, ChannelRole.HELP))
                .map(Channel::getMention)
                .map(mention ->
                        String.format(SimpleMessage.COMMAND_IN_WRONG_CHANNEL_MESSAGE_FORMAT.getMessage(), mention))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20059));

        event.reply()
                .withEmbeds(EmbedBuilder.buildSimpleMessage(messageString, EmbedType.SIMPLE_MESSAGE_EMBED_TYPE))
                .withEphemeral(true)
                .subscribe();

        log.info(
                "Command {} failed in wrong channel {} by user {} in guild {}",
                command.getCommandName(),
                event.getInteraction().getChannelId().asString(),
                event.getInteraction()
                        .getMember()
                        .map(Member::getId)
                        .map(Snowflake::asString)
                        .orElse("NULL"),
                event.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL"));
    }
}
