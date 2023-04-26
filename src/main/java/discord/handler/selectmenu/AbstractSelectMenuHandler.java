package discord.handler.selectmenu;

import discord.exception.main.EmptyOptionalException;
import discord.handler.DiscordEventHandler;
import discord.logging.LogMessage;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;

import java.util.Optional;

public abstract class AbstractSelectMenuHandler implements DiscordEventHandler<SelectMenuInteractionEvent> {

    private Optional<ActionRow> disableEventSelectMenuWithDefaultValue(SelectMenuInteractionEvent event, String value) {
        final var messageOpt = event.getInteraction().getMessage();
        if(messageOpt.isEmpty()) {
            return Optional.empty();
        }

        SelectMenu selectMenu = (SelectMenu) messageOpt.get().getComponents().get(0)
                .getChildren().get(0);
        var defaultOption = selectMenu.getOptions()
                .stream()
                .filter(option -> option.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20006));

        final var actionRow = ActionRow.of(
                SelectMenu.of(
                        selectMenu.getCustomId(),
                        SelectMenu.Option.ofDefault(defaultOption.getLabel(), "null")
                ).disabled()
        );

        return Optional.of(actionRow);
    }

    public void disableAndEditCurrentSelectMenu(SelectMenuInteractionEvent event, String value) {
        disableEventSelectMenuWithDefaultValue(event, value)
                .ifPresent(disabledSelectMenu -> event.edit().withComponents(disabledSelectMenu).subscribe());
    }

    String getOptionValueFromEvent(SelectMenuInteractionEvent event) {
        return event
                .getValues()
                .toString().replace("[", "").replace("]", "");
    }

}
