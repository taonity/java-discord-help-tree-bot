package org.taonity.helpbot.discord.event.command.positive.question.selectmenu;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.MessageChannelService;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.localisation.LocalizedMessage;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import org.taonity.helpbot.discord.mdc.OnCompleteSignalListenerBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TreeSelectMenuHandler extends AbstractSelectMenuHandler {

    private final SelectMenuService selectMenuService;
    private final MessageChannelService channelService;
    private final EventPredicates eventPredicates;

    @Override
    public final List<Function<SelectMenuInteractionEvent, Mono<Boolean>>> getFilterPredicates() {
        return Arrays.asList(
                eventPredicates::filterBot,
                e -> eventPredicates.filterByChannelRole(e, ChannelRole.HELP),
                this::isRelatedToTreeSelectMenu);
    }

    @Override
    public Mono<Void> handle(SelectMenuInteractionEvent event) {
        return event.getInteraction()
                .getGuild()
                .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20038)))
                .flatMap(guild -> channelService
                        .getChannel(guild, ChannelRole.HELP)
                        .flatMap(messageChannel -> {
                            final String optionValue = getOptionValueFromEvent(event);

                            return Mono.when(
                                            sendAnswerOrSelectMenu(messageChannel, event, guild, optionValue),
                                            disableAndEditCurrentSelectMenu(event, optionValue))
                                    .tap(OnCompleteSignalListenerBuilder.of(
                                            () -> log.info("Select menu was selected with node {}", optionValue)));
                        }));
    }

    private Mono<Message> sendAnswerOrSelectMenu(
            MessageChannel messageChannel, SelectMenuInteractionEvent event, Guild guild, String optionValue) {
        final var guildId = guild.getId().asString();

        final var smManagerOpt = getSmManager(event, guildId);
        if (smManagerOpt.isEmpty()) {
            return Mono.empty();
        }
        final var smManager = smManagerOpt.get();
        if (smManager.atLastQuestionInBranch()) {
            selectMenuService.configureSmManagerAnswerStage(smManager, guildId);

            return messageChannel
                    .createMessage(smManager.getTranslatedAnswerText())
                    .tap(OnCompleteSignalListenerBuilder.of(() -> log.info(
                            "Select menu answer {} entered in {} stage",
                            smManager.getAnswerNode().getId(),
                            smManager.getAnswerNode().getNodeFunction().name())));
        } else {
            final SelectMenu selectMenu = smManager.createNextTreeSelectMenu(optionValue);
            smManager.updateLastUpdateTime();

            return messageChannel
                    .createMessage(LocalizedMessage.CLARIFICATION_MESSAGE.translate(smManager.getLanguage()))
                    .withComponents(ActionRow.of(selectMenu))
                    .tap(OnCompleteSignalListenerBuilder.of(() -> log.info("Next select menu was sent")));
        }
    }

    private Mono<Boolean> isRelatedToTreeSelectMenu(SelectMenuInteractionEvent event) {
        final var guildId = event.getInteraction()
                .getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20038));

        final var smManagerOpt = getSmManager(event, guildId);
        return Mono.just(smManagerOpt
                .filter(smManager -> isRelatedToTreeSmMandager(event, smManager))
                .isPresent());
    }

    private static boolean isRelatedToTreeSmMandager(SelectMenuInteractionEvent event, SelectMenuManager smManager) {
        return event.getCustomId().equals(smManager.getTreeSelectMenuCustomId());
    }

    private Optional<SelectMenuManager> getSmManager(SelectMenuInteractionEvent event, String guildId) {
        return event.getInteraction()
                .getMember()
                .map(Member::getId)
                .map(memberId -> selectMenuService.getSmManager(memberId, guildId))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20062));
    }
}
