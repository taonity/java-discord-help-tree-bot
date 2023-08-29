package discord.automation.steps;

import static org.assertj.core.api.Assertions.assertThat;

import discord.automation.utils.DbTablePrinter;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class DbStepDefinitions {

    private final JdbcTemplate jdbcTemplate;
    private final DbTablePrinter dbTablePrinter;

    @Then("User gitea data is present in DB")
    public void userGiteaDataPresentInDb(DataTable table) {
        final var rows = table.asMaps(String.class, String.class);
        rows.forEach(this::userGiteaDataPresentInDb);
    }

    private void userGiteaDataPresentInDb(Map<String, String> map) {
        final var quantityOfUserData = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM guild_settings " + "WHERE guild_id = ? "
                        + "AND gitea_user_id = ? "
                        + "AND gitea_user_alphanumeric = ?",
                Integer.class,
                map.get("guildId"),
                Integer.valueOf(map.get("giteaUserId")),
                map.get("giteaUserAlphaNumeric"));
        assertThat(quantityOfUserData)
                .overridingErrorMessage(makeErrorMessage("guild_settings", 1))
                .isEqualTo(1);
    }

    @Then("User channel data is updated in DB")
    public void userChannelDataUpdatedInDb(DataTable table) {
        final var rows = table.asMaps(String.class, String.class);
        rows.forEach(this::updateRowIntoGiteaUserTable);
    }

    private void updateRowIntoGiteaUserTable(Map<String, String> map) {
        jdbcTemplate.update(
                "UPDATE guild_settings SET " + "log_channel_id = ?, " + "help_channel_id = ? " + "WHERE guild_id = ?",
                map.get("logChannelId"),
                map.get("helpChannelId"),
                map.get("guildId"));
    }

    @Then("User channel data is present in DB")
    public void userChannelDataPresentInDb(DataTable table) {
        final var rows = table.asMaps(String.class, String.class);
        rows.forEach(this::userChannelDataPresentInDb);
    }

    private void userChannelDataPresentInDb(Map<String, String> map) {
        final var quantityOfUserData = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM guild_settings " + "WHERE guild_id = ? "
                        + "AND log_channel_id = ? "
                        + "AND help_channel_id = ?",
                Integer.class,
                map.get("guildId"),
                map.get("logChannelId"),
                map.get("helpChannelId"));

        assertThat(quantityOfUserData)
                .overridingErrorMessage(makeErrorMessage("guild_settings", 1))
                .isEqualTo(1);
    }

    private String makeErrorMessage(String tableName, int expectedResult) {
        final var tableString = dbTablePrinter.print(tableName);
        return String.format("Expected %s. Table looks like:\n%s", expectedResult, tableString);
    }
}
