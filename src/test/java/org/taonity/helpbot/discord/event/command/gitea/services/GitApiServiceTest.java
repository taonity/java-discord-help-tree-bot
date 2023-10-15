package org.taonity.helpbot.discord.event.command.gitea.services;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.taonity.helpbot.config.PropertyConfig;

@ContextConfiguration(
        initializers = ConfigDataApplicationContextInitializer.class,
        classes = {GitApiService.class, PropertyConfig.class})
@ExtendWith(SpringExtension.class)
public class GitApiServiceTest {

    @Autowired
    GitApiService gitApiService;

    @Test
    @Disabled
    public void squashCommits() {
        gitApiService.squashCommits("user_lsXF", "repo_lsXF", 1, "448934652992946176");
    }
}
