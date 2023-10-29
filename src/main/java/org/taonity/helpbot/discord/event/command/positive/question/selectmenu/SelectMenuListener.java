package org.taonity.helpbot.discord.event.command.positive.question.selectmenu;

import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import java.util.Collection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.event.DiscordEventListener;
import org.taonity.helpbot.discord.event.ExtendedDiscordEventListener;
import org.taonity.helpbot.discord.event.MdcAwareThreadPoolExecutor;
import org.taonity.helpbot.discord.event.Slf4jRunnable;
import reactor.core.publisher.Flux;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class SelectMenuListener implements ExtendedDiscordEventListener<SelectMenuInteractionEvent> {

    private final Collection<AbstractSelectMenuHandler> handlers;

    private final MdcAwareThreadPoolExecutor mdcAwareThreadPoolExecutor;

    @Override
    public Slf4jRunnable<SelectMenuInteractionEvent> createSlf4jRunnable(SelectMenuInteractionEvent event) {
        return new Slf4jSelectMenuInteractionEventRunnable(event);
    }
}
