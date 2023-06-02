package discord.automation.config;

import discord.dto.gitea.api.SearchResult;
import discord.repository.GuildSettingsRepository;
import discord.services.GiteaUserService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GiteaUserTestService {

    private static final String FILE_NAME = "dialog-starter.yaml";
    private static final String BRANCH_NAME = "main";

    private final GiteaApiTestService giteaApiTestService;
    private final GuildSettingsRepository guildSettingsRepository;

    public String getGiteaUserFileContent(String guildId) {
        final var guildSettings = guildSettingsRepository
                .findGuildSettingByGuildId(guildId)
                .orElseThrow(() -> new RuntimeException("Failed to retrieve guild settings from db"));

        final var giteaRepoName = Optional.of(giteaApiTestService.getReposByUid(guildSettings.getGiteaUserId()))
                .filter(SearchResult::isOk)
                .map(SearchResult::getData)
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve gitea repos"))
                .get(0)
                .getName();

        final var giteaUserName =
                String.format(GiteaUserService.USER_NAME_FORMAT, guildSettings.getGiteaUserAlphanumeric());

        return giteaApiTestService
                .getFile(giteaUserName, giteaRepoName, FILE_NAME, BRANCH_NAME)
                .getContentAsString();
    }
}
