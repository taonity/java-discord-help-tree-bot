package discord.controllers;

import discord.dao.GithubWebhookEvent;
import discord.exception.TreeRootValidationException;
import discord.services.DialogNotificationService;
import discord.tree.TreeRoot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GitHubWebHookController {

    private final TreeRoot treeRoot;
    private final DialogNotificationService dialogNotificationService;

    @PostMapping(value = "/dialog-push")
    @ResponseStatus(code = HttpStatus.OK)
    public void webHook(@RequestBody GithubWebhookEvent event) {
        final String githubSenderUserLogin = event.getSender().getLogin();
        try {
            treeRoot.updateRoot();
            dialogNotificationService.sendNotification(githubSenderUserLogin, "Success");
        } catch (TreeRootValidationException e) {
            dialogNotificationService.sendNotification(githubSenderUserLogin, e.getMessage());
        }
        System.out.println("It works!");
        System.out.println(event);
    }

}
