package discord.automation.hooks;

import discord.automation.services.GiteaUserTestService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GiteaHooks {
    private final GiteaUserTestService giteaUserTestService;

    @Before(value = "@GiteaRepoReset")
    @After(value = "@GiteaRepoReset")
    public void clearAddedData() {
        giteaUserTestService.resetRepo("448934652992946176");
    }
}
