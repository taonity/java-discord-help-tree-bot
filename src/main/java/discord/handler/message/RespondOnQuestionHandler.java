package discord.handler.message;

import discord.exception.EmptyOptionalException;
import discord.exception.NullObjectException;
import discord.handler.EventPredicates;
import discord.localisation.LogMessage;
import discord.model.GuildSettings;
import discord.services.SelectMenuService;
import discord.utils.SelectMenuManager;
import discord.structure.UserStatus;
import discord.structure.ChannelRole;
import discord.services.MessageChannelService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.MessageCreateSpec;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class RespondOnQuestionHandler implements MessageHandler {
    private final MessageChannelService messageChannelService;
    private final SelectMenuService selectMenuService;
    private final GatewayDiscordClient client;
    private final EventPredicates eventPredicates;
    private final GuildSettings guildSettings;

    @Override
    public boolean filter(MessageCreateEvent event) {
        return Stream.of(event)
                .filter(e -> eventPredicates.filterByChannelId(event, guildSettings.getHelpChannelId()))
                .filter(eventPredicates::filterBot)
                .count() == 1;
    }

    @Override
    public void handle(MessageCreateEvent event) {
        final var author = event.getMessage().getAuthor();
        if (author.isEmpty()) {
            throw new EmptyOptionalException(LogMessage.ALERT_20011, messageChannelService);
        }

        final var smManagerOpt = selectMenuService.getSmManagerByUserId(author.get().getId());
        if (smManagerOpt.isEmpty()) {
            throw new EmptyOptionalException(LogMessage.ALERT_20012, messageChannelService);
        }
        final var smManager = smManagerOpt.get();

        final var targetId = smManager.getTargetId();
        final var targetUser = client.getUserById(Snowflake.of(targetId)).block();
        if (isNull(targetUser)) {
            throw new NullObjectException(LogMessage.ALERT_20013, messageChannelService);
        }

        selectMenuService.completeSmManagerReturnTextStage(smManager);

        messageChannelService.getChannel(ChannelRole.HELP).createMessage(
                MessageCreateSpec.builder()
                        .messageReference(event.getMessage().getId())
                        .content(targetUser.getMention())
                        .build()
        ).subscribe();
    }
}
