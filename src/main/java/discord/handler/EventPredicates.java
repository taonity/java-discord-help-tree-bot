package discord.handler;

import discord.exception.EmptyOptionalException;
import discord.exception.NullObjectException;
import discord.localisation.LogMessage;
import discord.services.MessageChannelService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class EventPredicates {
    final private MessageChannelService messageChannelService;

    public boolean filterByChannelId(InteractionCreateEvent event, String channelId) {
        var channel = event.getInteraction()
                .getChannel()
                .block();
        if(isNull(channel)) {
            throw new NullObjectException(LogMessage.ALERT_20007, messageChannelService);
        }
        return channel.getId()
                .asString()
                .equals(channelId);
    }

    public boolean filterByChannelId(MessageCreateEvent event, String channelId) {
        var channel = event.getMessage()
                .getChannel()
                .block();
        if(isNull(channel)) {
            throw new NullObjectException(LogMessage.ALERT_20008, messageChannelService);
        }
        return channel.getId()
                .asString()
                .equals(channelId);
    }

    public boolean filterByAuthorId(InteractionCreateEvent event, List<String> userWhiteList) {
        var member = event.getInteraction().getMember();
        if(member.isEmpty()) {
            throw new EmptyOptionalException(LogMessage.ALERT_20009, messageChannelService);
        }
        final String authorId = member.get().getId().asString();
        return userWhiteList.contains(authorId);
    }

    public boolean filterBot(MessageCreateEvent event) {
        final var messageAuthor = event.getMessage().getAuthor();
        if (messageAuthor.isEmpty()) {
            throw new EmptyOptionalException(LogMessage.ALERT_20003, messageChannelService);
        }
        return !messageAuthor.get().isBot();
    }
}
