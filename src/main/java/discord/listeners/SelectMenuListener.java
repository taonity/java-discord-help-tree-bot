package discord.listeners;

import discord.handler.selectmenu.AbstractSelectMenuHandler;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectMenuListener implements DiscordEventListener<SelectMenuInteractionEvent> {

    private final Collection<AbstractSelectMenuHandler> selectMenuHandlerCollection;

    public void handle(SelectMenuInteractionEvent event) {
        Flux.fromIterable(selectMenuHandlerCollection)
                .filter(selectMenu -> selectMenu.filter(event))
                .next()
                .flatMap(selectMenu -> selectMenu.reactiveHandle(event))
                .subscribe();
    }
}
