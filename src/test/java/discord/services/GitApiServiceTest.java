package discord.services;

import discord.config.PropertyConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class, classes = {GitApiService.class, PropertyConfig.class})
@RunWith(SpringRunner.class)
public class GitApiServiceTest {

    @Autowired
    GitApiService gitApiService;

    @Test
    public void squashCommits() {
        gitApiService.squashCommits("user_448934652992946176", "repo_448934652992946176", 3);
    }
}