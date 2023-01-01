package discord.controllers;

import discord.dto.WebhookEvent;
import discord.tree.TreeRootService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GitHubWebHookController {

    private final TreeRootService treeRootService;

    @PostMapping(value = "/dialog-push")
    @ResponseStatus(code = HttpStatus.OK)
    public void webHook(@RequestBody WebhookEvent event) {
        System.out.println("bruh");
        treeRootService.updateRoot(event);

    }

}
