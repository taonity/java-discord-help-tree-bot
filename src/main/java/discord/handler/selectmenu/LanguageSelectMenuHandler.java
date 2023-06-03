package discord.handler.selectmenu;

import static discord.localisation.LocalizedMessage.GREETING_MESSAGE;

import discord.exception.main.EmptyOptionalException;
import discord.handler.EventPredicates;
import discord.localisation.Language;
import discord.logging.LogMessage;
import discord.services.MessageChannelService;
import discord.services.SelectMenuService;
import discord.structure.ChannelRole;
import discord.utils.SelectMenuManager;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.entity.Member;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LanguageSelectMenuHandler extends AbstractSelectMenuHandler {

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
                                .filter(e -> isLanguageSelectMenu(selectMenuManager, e))
                                .count()
                        == 1)
                .isPresent();
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
        final var localizedMessage = GREETING_MESSAGE.translate(language);

        helpChannel
                .createMessage(localizedMessage)
                .withComponents(ActionRow.of(selectMenu))
                .subscribe();

        disableAndEditCurrentSelectMenu(event, optionValue);

        log.info(
                "Language select menu was set to {} by user {} in guild {}",
                optionValue,
                smManager.getUserId().asString(),
                event.getInteraction().getGuildId().map(Snowflake::asString).orElse("NULL"));
    }

    private Optional<SelectMenuManager> getSmManager(SelectMenuInteractionEvent event, String guildId) {
        return event.getInteraction()
                .getMember()
                .map(Member::getId)
                .map(memberId -> selectMenuService.getSmManager(memberId, guildId))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20051));
    }

    private boolean isLanguageSelectMenu(SelectMenuManager smManager, SelectMenuInteractionEvent event) {
        return event.getCustomId().equals(smManager.getLanguageSelectMenuCustomId());
    }
}
