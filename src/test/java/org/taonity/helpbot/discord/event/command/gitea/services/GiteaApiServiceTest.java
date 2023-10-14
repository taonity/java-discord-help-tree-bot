package org.taonity.helpbot.discord.event.command.gitea.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.taonity.helpbot.config.PropertyConfig;
import org.taonity.helpbot.discord.event.command.gitea.api.CreateFileOption;
import org.taonity.helpbot.discord.event.command.gitea.api.CreateRepoOption;
import org.taonity.helpbot.discord.event.command.gitea.api.CreateUserOption;
import org.taonity.helpbot.discord.logging.exception.GiteaApiException;

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
        assertThat(giteaApiService
                        .getFile(username, repo, filepath, branch_name)
                        .getContentAsString())
                .isEqualTo(fileContent);
    }
}
