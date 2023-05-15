package discord.automation.steps;

import discord.exception.GiteaApiException;
import discord.exception.main.EmptyOptionalException;
import discord.logging.LogMessage;
import discord.repository.AppSettingsRepository;
import discord.repository.GuildSettingsRepository;
import discord.services.GiteaApiService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ApplicationCommandData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.Assert.assertEquals;

class IsItFriday {
    static String isItFriday(String today) {
        return "Friday".equals(today) ? "TGIF" : "Nope";
    }
}


public class StepDefinitions {
    private String today;
    private String actualAnswer;

    @Autowired
    AppSettingsRepository appSettingsRepository;

    @Autowired
    GiteaApiService giteaApiService;

    @Autowired
    GuildSettingsRepository guildSettingsRepository;

    @Autowired
    GatewayDiscordClient automationClient;

    @Autowired
    Guild guild;

    @Autowired
    MessageChannel automationChannel;

    @Given("today is {string}")
    public void today_is(String today) throws GiteaApiException {
        System.out.println(appSettingsRepository.findOne().get());
        final var guildSettings = guildSettingsRepository.findGuildSettingByGuildId("448934652992946176").get();
        System.out.println(giteaApiService.getUserByUid(guildSettings.getGiteaUserId()));
        final long applicationId = automationClient.getRestClient().getApplicationId().blockOptional()
                .orElseThrow(() -> new RuntimeException("Failed to retrieve application id"));
        var id = automationClient.getRestClient().getApplicationService()
                .getGuildApplicationCommands(applicationId, guild.getId().asLong())
                .collectList()
                .blockOptional()
                .orElseThrow(() -> new RuntimeException("Failed to retrieve commands"))
                .stream()
                .filter(command -> command.name().equals("question"))
                .map(ApplicationCommandData::id)
                .map(Id::asLong)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Can't find a command"));

        automationChannel.createMessage().withContent("/question").block();

        this.today = today;
    }

    @When("I ask whether it's Friday yet")
    public void i_ask_whether_it_s_Friday_yet() {
        actualAnswer = IsItFriday.isItFriday(today);
    }

    @Then("I should be told {string}")
    public void i_should_be_told(String expectedAnswer) {
        assertEquals(expectedAnswer, actualAnswer);
    }
}
