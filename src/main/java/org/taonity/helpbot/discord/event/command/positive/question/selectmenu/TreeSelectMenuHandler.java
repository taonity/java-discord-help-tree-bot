package org.taonity.helpbot.discord.event.command.positive.question.selectmenu;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Member;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.MessageChannelService;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.localisation.LocalizedMessage;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TreeSelectMenuHandler extends AbstractSelectMenuHandler {

    private final SelectMenuService selectMenuService;
    private final MessageChannelService channelService;
    private final EventPredicates eventPredicates;

    @Override
    public boolean filter(SelectMenuInteractionEvent event) {
        final var guildId = event.getInteraction()
                .getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20038));

        final var smManagerOpt = getSmManager(event, guildId);

        return smManagerOpt
                .filter(selectMenuManager -> Stream.of(event)
                                .filter(eventPredicates::filterBot)
                                .filter(e -> eventPredicates.filterByChannelRole(event, ChannelRole.HELP))
                                .filter(e -> isTreeSelectMenu(selectMenuManager, e))
                                .count()
                        == 1)
                .isPresent();
    }

    @Override
    public void handle(SelectMenuInteractionEvent event) {
        final var guild = event.getInteraction()
                .getGuild()
                .blockOptional()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20038));
        final var guildId = guild.getId().asString();

        final var smManagerOpt = getSmManager(event, guildId);
        if (smManagerOpt.isEmpty()) {
            return;
        }
        final var smManager = smManagerOpt.get();

        final String optionValue = getOptionValueFromEvent(event);

        final var helpChannel = channelService.getChannel(guild, ChannelRole.HELP);
        // TODO move to else
        final SelectMenu selectMenu = smManager.createNextTreeSelectMenu(optionValue);

        final var guildIdLogValue =
                event.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL");
        log.info("Select menu was selected with node {}", optionValue);

        if (smManager.atLastQuestionInBranch()) {
            selectMenuService.configureSmManagerAnswerStage(smManager, guildId);

            helpChannel.createMessage(smManager.getTranslatedAnswerText()).subscribe();

            log.info(
                    "Select menu answer {} entered in {} stage",
                    smManager.getAnswerNode().getId(),
                    smManager.getAnswerNode().getNodeFunction().name());
        } else {
            smManager.updateLastUpdateTime();

            helpChannel
                    .createMessage(LocalizedMessage.CLARIFICATION_MESSAGE.translate(smManager.getLanguage()))
                    .withComponents(ActionRow.of(selectMenu))
                    .subscribe();

            log.info("Next select menu was sent");
        }

        disableAndEditCurrentSelectMenu(event, optionValue);
    }

    private boolean isTreeSelectMenu(SelectMenuManager smManager, SelectMenuInteractionEvent event) {
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
