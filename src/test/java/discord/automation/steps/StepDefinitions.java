package discord.automation.steps;

import discord.automation.config.GiteaApiTestService;
import discord.automation.config.GiteaUserTestService;
import discord.exception.GiteaApiException;
import discord.exception.main.EmptyOptionalException;
import discord.logging.LogMessage;
import discord.repository.AppSettingsRepository;
import discord.repository.GuildSettingsRepository;
import discord.services.GiteaApiService;
import discord.utils.ResourceFileLoader;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ApplicationCommandData;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.SoftAssertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import static org.junit.Assert.assertEquals;

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
        final var guildSettings = guildSettingsRepository.findGuildSettingByGuildId(map.get("guildId"))
                .orElseThrow(() -> new RuntimeException("Failed to retrieve guild setting"));
        final var expectedDialogFileContent = ResourceFileLoader.loadFile(map.get("dialogFile"));
        final var actualDialogFileContent = giteaUserTestService.getGiteaUserFileContent(map.get("guildId"));

        final var softly = new SoftAssertions();
        softly.assertThat(guildSettings.getGiteaUserId()).isEqualTo(map.get("giteaUserId"));
        softly.assertThat(guildSettings.getGiteaUserAlphanumeric()).isEqualTo(map.get("giteaUserAlphaNumeric"));
        softly.assertThat(actualDialogFileContent).isEqualTo(expectedDialogFileContent);
    }
}
