package discord.handler.message;

import discord.exception.main.EmptyOptionalException;
import discord.handler.EventPredicates;
import discord.logging.LogMessage;
import discord.services.MessageChannelService;
import discord.services.SelectMenuService;
import discord.structure.ChannelRole;
import discord.utils.SelectMenuManager;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageCreateSpec;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
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
                .filter(eventPredicates::filterIfIsGuildChannel)
                        .filter(e -> eventPredicates.filterIfChannelExistsInSettings(e, ChannelRole.HELP))
                        .filter(eventPredicates::filterEmptyAuthor)
                        .filter(eventPredicates::filterBot)
                        .filter(e -> eventPredicates.filterByChannelRole(e, ChannelRole.HELP))
                        .filter(e -> this.getSmManager(event).isPresent())
                        .count()
                == 1;
    }

    @Override
    public void handle(MessageCreateEvent event) {
        final var guildId = getGuildId(event);
        final var smManager = getSmManager(event).orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20012));

        final var targetId = smManager.getTargetId();
        final var targetUser = client.getUserById(Snowflake.of(targetId))
                .blockOptional()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20013));

        selectMenuService.removeSmManager(smManager, guildId);

        messageChannelService
                .getChannel(event.getGuild().block(), ChannelRole.HELP)
                .createMessage(MessageCreateSpec.builder()
                        .messageReference(event.getMessage().getId())
                        // TODO: maybe add client log
                        .content(targetUser.getMention())
                        .build())
                .subscribe();

        log.info(
                "Respond came on question with mention of {} by user {} in guild {}",
                targetUser.getId().asString(),
                smManager.getUserId().asString(),
                event.getGuild()
                        .blockOptional()
                        .map(Guild::getId)
                        .map(Snowflake::asString)
                        .orElse("NULL"));
    }

    private String getGuildId(MessageCreateEvent event) {
        return event.getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20038));
    }

    private Optional<SelectMenuManager> getSmManager(MessageCreateEvent event) {
        final var guildId = getGuildId(event);
        final var authorId = event.getMessage()
                .getAuthor()
                .map(User::getId)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20011));

        return selectMenuService.getSmManager(authorId, guildId);
    }
}
