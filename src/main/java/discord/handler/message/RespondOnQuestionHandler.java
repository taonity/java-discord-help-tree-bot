package discord.handler.message;

import discord.exception.EmptyOptionalException;
import discord.exception.NullObjectException;
import discord.handler.EventPredicates;
import discord.localisation.LogMessage;
import discord.services.SelectMenuService;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageCreateSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class RespondOnQuestionHandler implements MessageHandler {
    private final MessageChannelService messageChannelService;
    private final SelectMenuService selectMenuService;
    private final GatewayDiscordClient client;
    private final EventPredicates eventPredicates;

    @Override
    public boolean filter(MessageCreateEvent event) {
        return Stream.of(event)
                .filter(e -> eventPredicates.filterIfChannelExistsInSettings(e, ChannelRole.HELP))
                .filter(eventPredicates::filterEmptyAuthor)
                .filter(eventPredicates::filterBot)
                .filter(e -> eventPredicates.filterByChannelRole(e, ChannelRole.HELP))
                .count() == 1;
    }

    @Override
    public void handle(MessageCreateEvent event) {
        final var guildId = event.getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20038));

        final var authorId = event.getMessage().getAuthor()
                .map(User::getId)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20011));

        final var smManager = selectMenuService.getSmManager(authorId, guildId)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20012));

        final var targetId = smManager.getTargetId();
        final var targetUser = client.getUserById(Snowflake.of(targetId)).block();
        if (isNull(targetUser)) {
            throw new NullObjectException(LogMessage.ALERT_20013);
        }

        selectMenuService.removeSmManager(smManager, guildId);

        messageChannelService.getChannel(event.getGuild().block(), ChannelRole.HELP)
                .createMessage(
                MessageCreateSpec.builder()
                        .messageReference(event.getMessage().getId())
                        // TODO: maybe add client log
                        .content(targetUser.getMention())
                        .build()
        ).subscribe();
    }
}
