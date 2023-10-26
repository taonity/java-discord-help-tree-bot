package org.taonity.helpbot.discord.event.command.positive.config;

import static org.taonity.helpbot.discord.logging.LogMessage.ALERT_20089;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.event.MdcAwareThreadPoolExecutor;
import org.taonity.helpbot.discord.event.command.tree.TreeRootService;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GiteaWebHookController {

    private final TreeRootService treeRootService;
    private final GuildSettingsRepository guildSettingsRepository;
    private final MdcAwareThreadPoolExecutor threadPoolExecutor;

    @PostMapping(value = "/dialog-push")
    @ResponseStatus(code = HttpStatus.OK)
    public void webHook(@RequestBody WebhookEvent event) {
        final var guildSettings = guildSettingsRepository
                .findGuildSettingByGiteaUserId(event.getPusher().getId())
                .orElseThrow(() -> new EmptyOptionalException(ALERT_20089));
        final var runnable = new Slf4jWebhookEventRunnable(event, guildSettings, this::webHookWithMdc);
        threadPoolExecutor.submit(runnable);

        webHookWithMdc(event);
    }

    private void webHookWithMdc(WebhookEvent event) {
        log.info("Dialog update request received with content: {}", event);
        treeRootService.updateRoot(event);
    }
}
