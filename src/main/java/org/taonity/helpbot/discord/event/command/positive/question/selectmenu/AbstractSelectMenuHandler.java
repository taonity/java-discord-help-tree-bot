package org.taonity.helpbot.discord.event.command.positive.question.selectmenu;

import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import java.util.Optional;
import org.taonity.helpbot.discord.event.command.DiscordEventHandler;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;

public abstract class AbstractSelectMenuHandler implements DiscordEventHandler<SelectMenuInteractionEvent> {

    private Optional<ActionRow> disableEventSelectMenuWithDefaultValue(SelectMenuInteractionEvent event, String value) {
        final var messageOpt = event.getInteraction().getMessage();
        if (messageOpt.isEmpty()) {
            return Optional.empty();
        }

        SelectMenu selectMenu = (SelectMenu)
                messageOpt.get().getComponents().get(0).getChildren().get(0);
        var defaultOption = selectMenu.getOptions().stream()
                .filter(option -> option.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20006));

        final var actionRow = ActionRow.of(
                SelectMenu.of(selectMenu.getCustomId(), SelectMenu.Option.ofDefault(defaultOption.getLabel(), "null"))
                        .disabled());

        return Optional.of(actionRow);
    }

    public void disableAndEditCurrentSelectMenu(SelectMenuInteractionEvent event, String value) {
        disableEventSelectMenuWithDefaultValue(event, value)
                .ifPresent(disabledSelectMenu ->
                        event.edit().withComponents(disabledSelectMenu).subscribe());
    }

    String getOptionValueFromEvent(SelectMenuInteractionEvent event) {
        return event.getValues().toString().replace("[", "").replace("]", "");
    }
}
