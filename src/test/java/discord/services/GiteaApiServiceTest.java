package discord.services;

import discord.automation.config.GiteaApiTestService;
import discord.automation.config.GiteaUserTestService;
import discord.config.PropertyConfig;
import discord.dto.gitea.api.CreateFileOption;
import discord.dto.gitea.api.CreateRepoOption;
import discord.dto.gitea.api.CreateUserOption;
import discord.exception.GiteaApiException;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@EntityScan("discord.model")
@EnableJpaRepositories(basePackages = {"discord.repository"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ContextConfiguration(
        initializers = {ConfigDataApplicationContextInitializer.class},
        classes = {PropertyConfig.class, GiteaApiService.class})
public class GiteaApiServiceTest {

    @Autowired
    GiteaApiService giteaApiService;

    @Test
    @Disabled
    public void testGiteaUserFlow() throws GiteaApiException, InterruptedException {
        final var username = "user_WtXF";
        final var email = "WtXF@d.d";
        final var repo = "repo_WtXF";
        final var filepath = "test_file.json";
        final var branch_name = "main";
        final var fileContent = "test_text";

        System.out.println(giteaApiService.createUser(new CreateUserOption(username, "d12345", email)));
        giteaApiService.createRepository(username, new CreateRepoOption(repo));
        giteaApiService.createFile(username, repo, filepath, new CreateFileOption(fileContent));
        Thread.sleep(500);
        assertThat(giteaApiService.getFile(username, repo, filepath, branch_name).getContentAsString())
                .isEqualTo(fileContent);
    }

}