package org.taonity.helpbot.discord.event.command.positive.question.selectmenu;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.entity.Member;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.MessageChannelService;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.localisation.Language;
import org.taonity.helpbot.discord.localisation.LocalizedMessage;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LanguageSelectMenuHandler extends AbstractSelectMenuHandler {

    private final SelectMenuService selectMenuService;
    private final MessageChannelService channelService;
    private final EventPredicates eventPredicates;

    @Override
    public final List<Predicate<SelectMenuInteractionEvent>> getFilterPredicates() {
        return Arrays.asList(
                eventPredicates::filterBot,
                e -> eventPredicates.filterByChannelRole(e, ChannelRole.HELP),
                this::isRelatedToLanguageSelectMenu
        );
    }

    @Override
    public void handle(SelectMenuInteractionEvent event) {
        final var guild = event.getInteraction()
                .getGuild()
                .blockOptional()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20073));

        final var smManagerOpt = getSmManager(event, guild.getId().asString());
        if (smManagerOpt.isEmpty()) {
            return;
        }
        final var smManager = smManagerOpt.get();

        final String optionValue = getOptionValueFromEvent(event);

        final var helpChannel = channelService.getChannel(guild, ChannelRole.HELP);

        final var language = Language.valueOfLanguage(optionValue);
        smManager.setLanguage(language);
        smManager.updateLastUpdateTime();
        final var selectMenu = smManager.createFirstTreeSelectMenu();
        final var localizedMessage = LocalizedMessage.GREETING_MESSAGE.translate(language);

        helpChannel
                .createMessage(localizedMessage)
                .withComponents(ActionRow.of(selectMenu))
                .subscribe();

        disableAndEditCurrentSelectMenu(event, optionValue);

        log.info("Language select menu was set to {}", optionValue);
    }

    private Optional<SelectMenuManager> getSmManager(SelectMenuInteractionEvent event, String guildId) {
        return event.getInteraction()
                .getMember()
                .map(Member::getId)
                .map(memberId -> selectMenuService.getSmManager(memberId, guildId))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20051));
    }

    private boolean isRelatedToLanguageSmManager(SelectMenuManager smManager, SelectMenuInteractionEvent event) {
        return event.getCustomId().equals(smManager.getLanguageSelectMenuCustomId());
    }

    private boolean isRelatedToLanguageSelectMenu(SelectMenuInteractionEvent event) {
        final var guildId = event.getInteraction()
                .getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20038));

        final var smManagerOpt = getSmManager(event, guildId);
        return smManagerOpt
                .filter(selectMenuManager -> isRelatedToLanguageSmManager(selectMenuManager, event))
                .isPresent();
    }
}
