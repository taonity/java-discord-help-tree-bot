package org.taonity.helpbot.discord.event.command.positive.question.selectmenu;

import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.event.DiscordEventListener;
import org.taonity.helpbot.discord.event.MdcAwareThreadPoolExecutor;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelectMenuListener implements DiscordEventListener<SelectMenuInteractionEvent> {

    private final Collection<AbstractSelectMenuHandler> selectMenuHandlerCollection;

    @Getter
    private final MdcAwareThreadPoolExecutor mdcAwareThreadPoolExecutor;

    @Override
    public Runnable createSlf4jRunnable(SelectMenuInteractionEvent event) {
        return new Slf4jSelectMenuInteractionEventRunnable(event, this::handle);
    }

    public void handle(SelectMenuInteractionEvent event) {
        Flux.fromIterable(selectMenuHandlerCollection)
                .filter(selectMenu -> selectMenu.filter(event))
                .next()
                .flatMap(selectMenu -> selectMenu.reactiveHandle(event))
                .subscribe();
    }
}
