package discord.handler.selectmenu;

import discord.exception.main.EmptyOptionalException;
import discord.handler.EventPredicates;
import discord.logging.LogMessage;
import discord.services.MessageChannelService;
import discord.services.SelectMenuService;
import discord.structure.ChannelRole;
import discord.utils.SelectMenuManager;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

import static discord.localisation.LocalizedMessage.CLARIFICATION_MESSAGE;

@Component
@RequiredArgsConstructor
public class TreeSelectMenuHandler extends AbstractSelectMenuHandler {

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
                .filter(e -> isTreeSelectMenu(smManagerOpt.get(), e))
                .count() == 1;
    }

    @Override
    public void handle(SelectMenuInteractionEvent event) {
        final var guild = event.getInteraction().getGuild().blockOptional()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20038));
        final var guildId = guild.getId().asString();

        final var smManagerOpt = getSmManager(event, guildId);
        if(smManagerOpt.isEmpty()) {
            return;
        }
        final var smManager = smManagerOpt.get();

        final String optionValue = getOptionValueFromEvent(event);

        final var helpChannel = channelService.getChannel(guild, ChannelRole.HELP);

        final SelectMenu selectMenu = smManager.createNextTreeSelectMenu(optionValue);

        if(smManager.atLastQuestionInBranch()) {
            selectMenuService.configureSmManagerAnswerStage(smManager, guildId);

            helpChannel.createMessage(smManager.getTranslatedText()).subscribe();
        } else {
            smManager.updateLastUpdateTime();

            helpChannel.createMessage(CLARIFICATION_MESSAGE.translate(smManager.getLanguage()))
                    .withComponents(ActionRow.of(selectMenu)).subscribe();
        }

        disableAndEditCurrentSelectMenu(event, optionValue);
    }

    private boolean isTreeSelectMenu(SelectMenuManager smManager, SelectMenuInteractionEvent event) {
        return event.getCustomId().equals(smManager.getTreeSelectMenuCustomId());
    }

    private Optional<SelectMenuManager> getSmManager(SelectMenuInteractionEvent event, String guildId) {
        return event.getInteraction().getMember()
                .map(Member::getId)
                .map(memberId -> selectMenuService.getSmManager(memberId, guildId))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20062));
    }
}
