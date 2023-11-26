package org.taonity.helpbot.discord.event.command.positive.question.selectmenu;

import discord4j.common.util.Snowflake;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.GuildSettings;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.event.command.positive.question.UserStatus;
import org.taonity.helpbot.discord.event.command.tree.TreeRootService;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SelectMenuService {
    private final Map<String, List<SelectMenuManager>> selectMenuManagersMap = new HashMap<>();
    private final TreeRootService treeRootService;
    private final GuildSettingsRepository guildSettingsRepository;

    public Mono<Void> init() {
        return guildSettingsRepository
                .findAll()
                .collectList()
                .doOnSuccess(guildSettings -> guildSettings.stream()
                        .map(GuildSettings::getGuildId)
                        .forEach(guildId -> selectMenuManagersMap.put(guildId, new ArrayList<>())))
                .then();
    }

    public Optional<SelectMenuManager> getSmManager(Snowflake userId, String guildId) {
        return selectMenuManagersMap.get(guildId).stream()
                .filter(manager -> userId.equals(manager.getUserId()))
                .findAny();
    }

    public void configureSmManagerAnswerStage(SelectMenuManager smManager, String guildId) {
        switch (smManager.getAnswerNode().getNodeFunction()) {
            case ASK_INPUT:
                smManager.setUserStatus(UserStatus.WRITES_MESSAGE);
                smManager.updateLastUpdateTime();
                break;
            case RETURN_TEXT:
                selectMenuManagersMap.get(guildId).remove(smManager);
                break;
        }
    }

    public void removeSmManager(SelectMenuManager smManager, String guildId) {
        selectMenuManagersMap.get(guildId).remove(smManager);
    }

    public void removeSmManagerList(String guildId) {
        selectMenuManagersMap.remove(guildId);
    }

    public void createSmManagerList(String guildId) {
        selectMenuManagersMap.put(guildId, new ArrayList<>());
    }

    public SelectMenuManager initNewManager(Snowflake authorId, String guildId) {
        final var selectMenuManagerList = selectMenuManagersMap.get(guildId);
        selectMenuManagerList.removeIf(SelectMenuManager::isDead);
        selectMenuManagerList.removeIf(manager -> authorId.equals(manager.getUserId()));

        final var selectMenuManager = new SelectMenuManager(authorId, treeRootService.getRootByGuildId(guildId));
        selectMenuManagerList.add(selectMenuManager);
        return selectMenuManager;
    }
}
