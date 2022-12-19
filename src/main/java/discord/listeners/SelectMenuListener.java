package discord.listeners;

import discord.exception.EmptyOptionalException;
import discord.handler.selectmenu.AbstractSelectMenuHandler;
import discord.localisation.LogMessage;
import discord.services.SelectMenuService;
import discord.structure.ChannelRole;
import discord.utils.SelectMenuManager;
import discord.localisation.Language;
import discord.services.MessageChannelService;
import discord.tree.TreeRootService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collection;

import static discord.localisation.LocalizedMessage.CLARIFICATION_MESSAGE;
import static discord.localisation.LocalizedMessage.GREETING_MESSAGE;

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
