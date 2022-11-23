package discord.listeners;

import discord.exception.EmptyOptionalException;
import discord.localisation.LogMessage;
import discord.services.SelectMenuService;
import discord.utils.SelectMenuManager;
import discord.structure.ChannelRole;
import discord.localisation.Language;
import discord.services.MessageChannelService;
import discord.tree.TreeRoot;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static discord.localisation.LocalizedMessage.CLARIFICATION_MESSAGE;
import static discord.localisation.LocalizedMessage.GREETING_MESSAGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectMenuListener implements DiscordEventListener<SelectMenuInteractionEvent> {
    public final SelectMenuService selectMenuService;
    public final TreeRoot treeRoot;
    public final GatewayDiscordClient client;
    public final MessageChannelService channelService;

    public void handle(SelectMenuInteractionEvent event) {
        final var smManagerOpt = getSmManagerByEvent(event);
        if(smManagerOpt.isEmpty()) {
            throw new EmptyOptionalException(LogMessage.ALERT_20003, channelService);
        }
        final SelectMenuManager smManager = smManagerOpt.get();

        final String optionValue = getOptionValueFromEvent(event);

        if(isLanguageSelectMenu(smManager, event)) {
            final var language = Language.valueOfLanguage(optionValue);
            smManager.setLanguage(language);
            smManager.updateLastUpdateTime();
            final var selectMenu = smManager.createFirstTreeSelectMenu();
            final var localizedMessage = GREETING_MESSAGE.translate(language);

            channelService.getChannel(ChannelRole.HELP)
                    .createMessage(localizedMessage)
                    .withComponents(ActionRow.of(selectMenu))
                    .subscribe();

        } else if(isTreeSelectMenu(smManager, event)) {
            final SelectMenu selectMenu = smManager.createNextTreeSelectMenu(optionValue);

            if(smManager.atLastQuestionInBranch()) {
                selectMenuService.configureSmManagerAnswerStage(smManager);

                channelService.getChannel(ChannelRole.HELP)
                        .createMessage(smManager.getTranslatedText()).subscribe();
            } else {
                smManager.updateLastUpdateTime();

                channelService.getChannel(ChannelRole.HELP)
                        .createMessage(CLARIFICATION_MESSAGE.translate(smManager.getLanguage()))
                        .withComponents(ActionRow.of(selectMenu)).subscribe();
            }
        }

        var disabledSelectMenuOpt = disableEventSelectMenuWithDefaultValue(event, optionValue);
        if(disabledSelectMenuOpt.isEmpty()) {
            throw new EmptyOptionalException(LogMessage.ALERT_20004, channelService);
        }

        event.edit().withComponents(disabledSelectMenuOpt.get()).subscribe();
    }

    private Optional<SelectMenuManager> getSmManagerByEvent(SelectMenuInteractionEvent event) {
        var member = event.getInteraction().getMember();
        if(member.isEmpty()) {
            log.info(LogMessage.ALERT_20004.name());
            return Optional.empty();
        }

        return selectMenuService.getSmManagerByUserId(member.get().getId());
    }

    private boolean isTreeSelectMenu(SelectMenuManager smManager, SelectMenuInteractionEvent event) {
        return event.getCustomId().equals(smManager.getTreeSelectMenuCustomId());
    }

    private boolean isLanguageSelectMenu(SelectMenuManager smManager, SelectMenuInteractionEvent event) {
        return event.getCustomId().equals(smManager.getLanguageSelectMenuCustomId());
    }

    private String getOptionValueFromEvent(SelectMenuInteractionEvent event) {
        return event
                .getValues()
                .toString().replace("[", "").replace("]", "");
    }

    private Optional<ActionRow> disableEventSelectMenuWithDefaultValue(SelectMenuInteractionEvent event, String value) {
        var message = event.getInteraction().getMessage();
        if(message.isEmpty()) {
            log.info(LogMessage.ALERT_20005.name());
            return Optional.empty();
        }

        SelectMenu selectMenu = (SelectMenu) message.get().getComponents().get(0)
                .getChildren().get(0);
        var defaultOption = selectMenu.getOptions()
                .stream()
                .filter(option -> option.getValue().equals(value))
                .findFirst();

        if(defaultOption.isEmpty()) {
            log.info(LogMessage.ALERT_20006.name());
            return Optional.empty();
        }

        return Optional.of(ActionRow.of(
                SelectMenu.of(
                        selectMenu.getCustomId(),
                        SelectMenu.Option.ofDefault(defaultOption.get().getLabel(), "null")
                ).disabled())
        );
    }
}
