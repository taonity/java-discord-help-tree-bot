package discord.controllers;

import discord.dao.WebhookEvent;
import discord.tree.TreeRootService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static discord.services.GiteaApiService.HOOK_PATH;

@RestController
@RequiredArgsConstructor
public class GitHubWebHookController {

    private final TreeRootService treeRootService;

    @PostMapping(value = HOOK_PATH)
    @ResponseStatus(code = HttpStatus.OK)
    public void webHook(@RequestBody WebhookEvent event) {

        treeRootService.updateRoot(event);

    }

}
