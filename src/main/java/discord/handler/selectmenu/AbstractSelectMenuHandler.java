package discord.handler.selectmenu;

import discord.exception.EmptyOptionalException;
import discord.handler.DiscordEventHandler;
import discord.localisation.LogMessage;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;

public abstract class AbstractSelectMenuHandler implements DiscordEventHandler<SelectMenuInteractionEvent> {

    private ActionRow disableEventSelectMenuWithDefaultValue(SelectMenuInteractionEvent event, String value) {
        var message = event.getInteraction().getMessage()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20005));


        SelectMenu selectMenu = (SelectMenu) message.getComponents().get(0)
                .getChildren().get(0);
        var defaultOption = selectMenu.getOptions()
                .stream()
                .filter(option -> option.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20006));


        return ActionRow.of(
                SelectMenu.of(
                        selectMenu.getCustomId(),
                        SelectMenu.Option.ofDefault(defaultOption.getLabel(), "null")
                ).disabled()
        );
    }

    public void disableAndEditCurrentSelectMenu(SelectMenuInteractionEvent event, String value) {
        final var disabledSelectMenu = disableEventSelectMenuWithDefaultValue(event, value);

        event.edit().withComponents(disabledSelectMenu).subscribe();
    }

    String getOptionValueFromEvent(SelectMenuInteractionEvent event) {
        return event
                .getValues()
                .toString().replace("[", "").replace("]", "");
    }

}
