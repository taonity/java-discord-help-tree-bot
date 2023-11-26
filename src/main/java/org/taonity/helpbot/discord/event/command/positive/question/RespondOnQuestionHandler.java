package org.taonity.helpbot.discord.event.command.positive.question;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageCreateSpec;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.MessageChannelService;
import org.taonity.helpbot.discord.MessageHandler;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.event.command.positive.question.selectmenu.SelectMenuManager;
import org.taonity.helpbot.discord.event.command.positive.question.selectmenu.SelectMenuService;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import org.taonity.helpbot.discord.mdc.OnCompleteSignalListenerBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RespondOnQuestionHandler implements MessageHandler {
    private final MessageChannelService messageChannelService;
    private final SelectMenuService selectMenuService;
    private final GatewayDiscordClient gatewayDiscordClient;
    private final EventPredicates eventPredicates;

    @Override
    public final List<Function<MessageCreateEvent, Mono<Boolean>>> getFilterPredicates() {
        return Arrays.asList(
                eventPredicates::filterBot,
                eventPredicates::filterIfIsGuildChannel,
                e -> eventPredicates.filterIfChannelExistsInSettings(e, ChannelRole.HELP),
                eventPredicates::filterEmptyAuthor,
                e -> eventPredicates.filterByChannelRole(e, ChannelRole.HELP),
                e -> Mono.just(this.getSmManager(e).isPresent()));
    }

    @Override
    public Mono<Void> handle(MessageCreateEvent event) {
        final var guildId = getGuildId(event);
        final var smManager = getSmManager(event).orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20012));

        final var targetId = smManager.getTargetId();
        return gatewayDiscordClient
                .getUserById(Snowflake.of(targetId))
                .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20013)))
                .flatMap(targetUser -> {
                    selectMenuService.removeSmManager(smManager, guildId);
                    return event.getGuild().flatMap(guild -> messageChannelService
                            .getChannel(guild, ChannelRole.HELP)
                            .flatMap(messageChannel -> messageChannel.createMessage(MessageCreateSpec.builder()
                                    .messageReference(event.getMessage().getId())
                                    // TODO: maybe add client log
                                    .content(targetUser.getMention())
                                    .build()))
                            .tap(OnCompleteSignalListenerBuilder.of(() -> log.info(
                                    "Respond came on question with mention of {} by user {} in guild {}",
                                    targetUser.getId().asString(),
                                    smManager.getUserId().asString(),
                                    event.getGuild()
                                            .blockOptional()
                                            .map(Guild::getId)
                                            .map(Snowflake::asString)
                                            .orElse("NULL")))));
                })
                .then();
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
