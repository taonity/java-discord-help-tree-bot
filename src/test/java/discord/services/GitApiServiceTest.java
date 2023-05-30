package discord.services;

import discord.config.PropertyConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class,
        classes = {GitApiService.class, PropertyConfig.class})
@ExtendWith(SpringExtension.class)
public class GitApiServiceTest {

    @Autowired
    GitApiService gitApiService;

    @Test
    @Disabled
    public void squashCommits() {
        gitApiService.squashCommits(
                "user_lsXF",
                "repo_lsXF",
                1,
                "448934652992946176");
    }
}