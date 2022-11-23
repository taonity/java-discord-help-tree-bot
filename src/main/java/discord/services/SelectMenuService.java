package discord.services;

import discord.structure.UserStatus;
import discord.tree.TreeRoot;
import discord.utils.SelectMenuManager;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SelectMenuService {
    private final List<SelectMenuManager> selectMenuManagers;
    public final TreeRoot treeRoot;

    public Optional<SelectMenuManager> getSmManagerByUserId(Snowflake userId) {
        return selectMenuManagers.stream()
                .filter(manager -> userId.equals(manager.getUserId()))
                .findAny();
    }

    public void configureSmManagerAnswerStage(SelectMenuManager smManager) {
        switch (smManager.getAnswerNode().getNodeFunction()) {
            case ASK_INPUT:
                smManager.setUserStatus(UserStatus.WRITES_MESSAGE);
                smManager.updateLastUpdateTime();
                break;
            case RETURN_TEXT:
                selectMenuManagers.remove(smManager);
                break;
        }
    }

    public void completeSmManagerReturnTextStage(SelectMenuManager smManager) {
        selectMenuManagers.remove(smManager);
    }

    public SelectMenuManager initNewManager(Snowflake authorId) {
        selectMenuManagers.removeIf(SelectMenuManager::isDead);
        selectMenuManagers.removeIf(manager -> authorId.equals(manager.getUserId()));

        var selectMenuManager = new SelectMenuManager(treeRoot, authorId);
        selectMenuManagers.add(selectMenuManager);
        return selectMenuManager;
    }
}
