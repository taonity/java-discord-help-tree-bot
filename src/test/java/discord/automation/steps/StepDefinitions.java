package discord.automation.steps;

import discord.automation.config.GiteaUserTestService;
import discord.repository.GuildSettingsRepository;
import discord.utils.ResourceFileLoader;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.SoftAssertions;

@RequiredArgsConstructor
public class StepDefinitions {

    private final GuildSettingsRepository guildSettingsRepository;

    private final GiteaUserTestService giteaUserTestService;

    @Then("Gitea user id, alphanumeric and dialog file content must match")
    public void checkGiteaDataTable(DataTable table) {
        final var rows = table.asMaps(String.class, String.class);
        rows.forEach(this::checkGiteaData);
    }

    private void checkGiteaData(Map<String, String> map) {
        final var guildSettings = guildSettingsRepository
                .findGuildSettingByGuildId(map.get("guildId"))
                .orElseThrow(() -> new RuntimeException("Failed to retrieve guild setting"));
        final var expectedDialogFileContent = ResourceFileLoader.loadFile(map.get("dialogFile"));
        final var actualDialogFileContent = giteaUserTestService.getGiteaUserFileContent(map.get("guildId"));

        final var softly = new SoftAssertions();
        softly.assertThat(guildSettings.getGiteaUserId()).isEqualTo(map.get("giteaUserId"));
        softly.assertThat(guildSettings.getGiteaUserAlphanumeric()).isEqualTo(map.get("giteaUserAlphaNumeric"));
        softly.assertThat(actualDialogFileContent).isEqualTo(expectedDialogFileContent);
    }
}
