package automation.services;

import static java.util.Objects.isNull;

import automation.services.dto.UpdateFileOption;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.GuildSettings;
import org.taonity.helpbot.discord.event.command.gitea.api.ContentsResponse;
import org.taonity.helpbot.discord.event.command.gitea.api.SearchResult;
import org.taonity.helpbot.discord.event.command.gitea.services.GiteaUserService;

@Component
@RequiredArgsConstructor
public class GiteaUserTestService {

    private static final String FILE_NAME = "dialog-starter.yaml";
    private static final String BRANCH_NAME = "main";

    private final GiteaApiTestService giteaApiTestService;
    private final JdbcTemplate jdbcTemplate;
    private final GitApiTestService gitApiTestService;

    public String getGiteaUserFileContent(String guildId) {
        return getGiteaUserFile(guildId).getContentAsString();
    }

    public void updateGiteaUserFileContent(String guildId, String newFileContent) {
        final var fileSha = getGiteaUserFile(guildId).getSha();
        final var guildSettings = retrieveGuildSettings(guildId);
        final var giteaRepoName = getGiteaRepoName(guildSettings);
        final var giteaUserName = getUserName(guildSettings);
        final var updateFileOption = new UpdateFileOption(newFileContent, fileSha);

        giteaApiTestService.updateFile(giteaUserName, giteaRepoName, FILE_NAME, updateFileOption);
    }

    private ContentsResponse getGiteaUserFile(String guildId) {
        final var guildSettings = retrieveGuildSettings(guildId);
        final var giteaRepoName = getGiteaRepoName(guildSettings);
        final var giteaUserName = getUserName(guildSettings);

        return giteaApiTestService.getFile(giteaUserName, giteaRepoName, FILE_NAME, BRANCH_NAME);
    }

    private static String getUserName(GuildSettings guildSettings) {
        return String.format(GiteaUserService.USER_NAME_FORMAT, guildSettings.getGiteaUserAlphanumeric());
    }

    private String getGiteaRepoName(GuildSettings guildSettings) {
        return Optional.of(giteaApiTestService.getReposByUid(guildSettings.getGiteaUserId()))
                .filter(SearchResult::isOk)
                .map(SearchResult::getData)
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve gitea repos"))
                .get(0)
                .getName();
    }

    private GuildSettings retrieveGuildSettings(String guildId) {
        final var giteaUserId = jdbcTemplate.queryForObject(
                "SELECT gitea_user_id FROM guild_settings WHERE guild_id = ?", Integer.class, guildId);
        final var giteaUserAlphanumeric = jdbcTemplate.queryForObject(
                "SELECT gitea_user_alphanumeric FROM guild_settings WHERE guild_id = ?", String.class, guildId);

        if (isNull(giteaUserId) || isNull(giteaUserAlphanumeric)) {
            throw new RuntimeException("Failed to retrieve guild settings from db");
        }
        return GuildSettings.builder()
                .giteaUserId(giteaUserId)
                .giteaUserAlphanumeric(giteaUserAlphanumeric)
                .build();
    }

    public void resetRepo(String guildId) {
        final var guildSettings = retrieveGuildSettings(guildId);
        final var giteaRepoName = getGiteaRepoName(guildSettings);
        final var giteaUserName = getUserName(guildSettings);

        gitApiTestService.resetRepo(giteaUserName, giteaRepoName, guildId);
    }
}
