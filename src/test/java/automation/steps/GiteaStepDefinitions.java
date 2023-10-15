package automation.steps;

import automation.services.GiteaUserTestService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.SoftAssertions;
import org.springframework.core.io.ClassPathResource;
import org.taonity.helpbot.discord.event.command.gitea.ResourceFileLoader;

@RequiredArgsConstructor
public class GiteaStepDefinitions {

    private final GiteaUserTestService giteaUserTestService;

    @Then("Tree config file is present in user gitea repo")
    public void treeConfigFilePresentInUserGiteaRepo(DataTable table) {
        final var rows = table.asMaps(String.class, String.class);
        rows.forEach(this::assertFileContentIsExpected);
    }

    private void assertFileContentIsExpected(Map<String, String> map) {
        final var actualDialogFileContent = giteaUserTestService.getGiteaUserFileContent(map.get("guildId"));
        final var expectedDialogFileContent = ResourceFileLoader.loadFile(map.get("dialogFile"));

        final var softly = new SoftAssertions();
        softly.assertThat(actualDialogFileContent).isEqualTo(expectedDialogFileContent);
    }

    @Then("Tree config file is updated in user gitea repo")
    public void updateTreeConfigFile(DataTable table) {
        final var rows = table.asMaps(String.class, String.class);
        rows.forEach(this::updateTreeConfigFile);
    }

    private void updateTreeConfigFile(Map<String, String> map) {
        final var resource = new ClassPathResource(map.get("dialogFile"));
        try {
            final var fileContent = resource.getContentAsString(StandardCharsets.UTF_8);
            giteaUserTestService.updateGiteaUserFileContent(map.get("guildId"), fileContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
