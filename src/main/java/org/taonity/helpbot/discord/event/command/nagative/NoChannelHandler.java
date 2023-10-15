package org.taonity.helpbot.discord.event.command.nagative;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Member;
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
import org.taonity.helpbot.discord.localisation.SimpleMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoChannelHandler extends AbstractSlashCommand {

    @Getter
    private final CommandName command = CommandName.ANY;

    private final EventPredicates eventPredicates;

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                        .filter(this::filterByCommand)
                        .filter(eventPredicates::filterBot)
                        .filter(e -> !eventPredicates.filterIfChannelsExistInSettings(e))
                        .count()
                == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {

        event.reply()
                .withEmbeds(EmbedBuilder.buildSimpleMessage(
                        SimpleMessage.NO_CHANNEL_MESSAGE.getMessage(), EmbedType.SIMPLE_MESSAGE_EMBED_TYPE))
                .withEphemeral(true)
                .withEphemeral(true)
                .subscribe();

        log.info(
                "Command {} failed with not configured channel by user {} in guild {}",
                event.getCommandName(),
                event.getInteraction()
                        .getMember()
                        .map(Member::getId)
                        .map(Snowflake::asString)
                        .orElse("NULL"),
                event.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL"));
    }
}
