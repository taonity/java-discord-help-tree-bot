package org.taonity.helpbot.discord.event.tip;

import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.MessageChannelService;
import org.taonity.helpbot.discord.MessageHandler;
import org.taonity.helpbot.discord.embed.EmbedBuilder;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SendTipOnPeriodicalMessageHandler implements MessageHandler {
    private static final int MIN_MESSAGE_LENGTH = 6;

    private final Notificator notificator = new Notificator();

    private final MessageChannelService channelService;
    private final EventPredicates eventPredicates;

    @Override
    public final List<Function<MessageCreateEvent, Mono<Boolean>>> getFilterPredicates() {
        return Arrays.asList(
                eventPredicates::filterBot,
                eventPredicates::filterIfIsGuildChannel,
                e -> eventPredicates.filterIfChannelExistsInSettings(e, ChannelRole.HELP),
                eventPredicates::filterEmptyAuthor,
                this::messageIsEnoughLarge,
                e -> notificator.isTime(),
                e -> eventPredicates.filterByChannelRole(e, ChannelRole.HELP));
    }

    private Mono<Boolean> messageIsEnoughLarge(MessageCreateEvent event) {
        return Mono.just(event.getMessage().getContent().length() > MIN_MESSAGE_LENGTH);
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        final var embed = EmbedBuilder.buildTipEmbed(notificator.getNotificationText());
        return event.getGuild()
                .flatMap(guild -> channelService.getChannel(guild, ChannelRole.HELP))
                .flatMap(helpChannel -> helpChannel.createMessage(embed))
                .then();
    }
}
