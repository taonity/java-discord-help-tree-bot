package org.taonity.helpbot.discord.event.command.positive.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.taonity.helpbot.discord.event.command.tree.TreeRootService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GiteaWebHookController {

    private final TreeRootService treeRootService;

    @PostMapping(value = "/dialog-push")
    @ResponseStatus(code = HttpStatus.OK)
    public void webHook(@RequestBody WebhookEvent event) {
        log.info("Dialog update request received with content: {}", event);
        treeRootService.updateRoot(event);
    }
}