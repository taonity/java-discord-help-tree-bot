package org.taonity.helpbot.discord.event.tip;

import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.MessageChannelService;
import org.taonity.helpbot.discord.MessageHandler;
import org.taonity.helpbot.discord.embed.EmbedBuilder;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;

@Component
@RequiredArgsConstructor
public class SendTipOnPeriodicalMessageHandler implements MessageHandler {
    private static final int MIN_MESSAGE_LENGTH = 6;

    private final Notificator notificator = new Notificator();

    private final MessageChannelService channelService;
    private final EventPredicates eventPredicates;

    @Override
    public final List<Predicate<MessageCreateEvent>> getFilterPredicates() {
        return Arrays.asList(
                eventPredicates::filterBot,
                eventPredicates::filterIfIsGuildChannel,
                e -> eventPredicates.filterIfChannelExistsInSettings(e, ChannelRole.HELP),
                eventPredicates::filterEmptyAuthor,
                this::messageIsEnoughLarge,
                e -> notificator.isTime(),
                e -> eventPredicates.filterByChannelRole(e, ChannelRole.HELP)
        );
    }

    private boolean messageIsEnoughLarge(MessageCreateEvent event) {
        return event.getMessage().getContent().length() > MIN_MESSAGE_LENGTH;
    }

    @Override
    public void handle(MessageCreateEvent event) {
        final var embed = EmbedBuilder.buildTipEmbed(notificator.getNotificationText());
        final var guild =
                event.getGuild().blockOptional().orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20072));
        final var helpChannel = channelService.getChannel(guild, ChannelRole.HELP);

        helpChannel.createMessage(embed).subscribe();
    }
}
