package discord.handler.selectmenu;

import discord.exception.main.EmptyOptionalException;
import discord.handler.EventPredicates;
import discord.localisation.Language;
import discord.localisation.LogMessage;
import discord.services.MessageChannelService;
import discord.services.SelectMenuService;
import discord.structure.ChannelRole;
import discord.utils.SelectMenuManager;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

import static discord.localisation.LocalizedMessage.GREETING_MESSAGE;

@Component
@RequiredArgsConstructor
public class LanguageSelectMenuHandler extends AbstractSelectMenuHandler {

    private final SelectMenuService selectMenuService;
    private final MessageChannelService channelService;
    private final EventPredicates eventPredicates;

    @Override
    public boolean filter(SelectMenuInteractionEvent event) {
        final var guildId = event.getInteraction().getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20038));

        final var smManagerOpt = getSmManager(event, guildId);
        if(smManagerOpt.isEmpty()) {
            return false;
        }

        return Stream.of(event)
                .filter(eventPredicates::filterBot)
                .filter(e -> eventPredicates.filterByChannelRole(event, ChannelRole.HELP))
                .filter(e -> isLanguageSelectMenu(smManagerOpt.get(), e))
                .count() == 1;
    }

    @Override
    public void handle(SelectMenuInteractionEvent event) {
        final var guild = event.getInteraction().getGuild().blockOptional()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20073));

        final var smManagerOpt = getSmManager(event, guild.getId().asString());
        if(smManagerOpt.isEmpty()) {
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

        helpChannel.createMessage(localizedMessage)
                .withComponents(ActionRow.of(selectMenu))
                .subscribe();

        disableAndEditCurrentSelectMenu(event, optionValue);
    }

    private Optional<SelectMenuManager> getSmManager(SelectMenuInteractionEvent event, String guildId) {
        return event.getInteraction().getMember()
                .map(Member::getId)
                .map(memberId -> selectMenuService.getSmManager(memberId, guildId))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20051));
    }

    private boolean isLanguageSelectMenu(SelectMenuManager smManager, SelectMenuInteractionEvent event) {
        return event.getCustomId().equals(smManager.getLanguageSelectMenuCustomId());
    }
}
