package discord.services;

import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord.structure.UserStatus;
import discord.tree.TreeRootService;
import discord.utils.SelectMenuManager;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class SelectMenuService {
    private final Map<String, List<SelectMenuManager>> selectMenuManagersMap = new HashMap<>();
    private final TreeRootService treeRootService;
    private final GuildSettingsRepository guildSettingsRepository;

    @PostConstruct
    private void postConstruct() {
        StreamSupport.stream(guildSettingsRepository.findAll().spliterator(), true)
                .map(GuildSettings::getGuildId)
                .forEach(guildId -> selectMenuManagersMap.put(guildId, new ArrayList<>()));
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

        var selectMenuManager = new SelectMenuManager(authorId, treeRootService.getRootByGuildId(guildId));
        selectMenuManagerList.add(selectMenuManager);
        return selectMenuManager;
    }
}
