package org.taonity.helpbot.discord.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.event.ListenerRegister;
import org.taonity.helpbot.discord.event.command.gitea.services.GiteaApiService;
import org.taonity.helpbot.discord.event.command.positive.config.GuildPersistableDataStartupService;
import org.taonity.helpbot.discord.event.command.positive.question.selectmenu.SelectMenuService;
import org.taonity.helpbot.discord.event.command.tree.TreeRootService;
import org.taonity.helpbot.discord.mdc.ContextRegistryMdcKeyRegister;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class InitSequenceRunner implements CommandLineRunner {
    private final TreeRootService treeRootService;
    private final SelectMenuService selectMenuService;
    private final GiteaApiService giteaApiService;
    private final GuildPersistableDataStartupService guildPersistableDataStartupService;
    private final ListenerRegister listenerRegister;

    @Override
    public void run(String... args) throws Exception {
        ContextRegistryMdcKeyRegister.init();
        Hooks.onOperatorDebug();
        Mono.empty()
                .then(giteaApiService.init())
                .then(guildPersistableDataStartupService.updatePersistableData())
                .then(Mono.when(treeRootService.init(), selectMenuService.init()))
                .doOnSuccess(result -> listenerRegister.init())
                .subscribe();
    }
}
