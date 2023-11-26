package org.taonity.helpbot.discord.event.command.positive.config;

import static org.taonity.helpbot.discord.logging.LogMessage.ALERT_20089;
import static org.taonity.helpbot.discord.mdc.ContextRegistryMdcKeyRegister.GUILD_ID_MDC_KEY;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.event.command.tree.TreeRootService;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import org.taonity.helpbot.discord.mdc.OnCompleteSignalListenerBuilder;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@CrossOrigin(origins = "http://localhost:9015")
@RestController
@RequiredArgsConstructor
@Slf4j
public class GiteaWebHookController {

    static final String FIRST_COMMIT_MESSAGE = "Add dialog-starter.yaml\n";

    private final TreeRootService treeRootService;
    private final GuildSettingsRepository guildSettingsRepository;

    @Value("${gitea.admin.username}")
    private String adminUsername;

    // TODO: secure endpoint
    @PostMapping(value = "/dialog-push")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<Void> webHook(@RequestBody WebhookEvent event) {
        if (event.getCommits().get(0).getMessage().equals(FIRST_COMMIT_MESSAGE)) {
            return Mono.empty();
        }
        return guildSettingsRepository
                .findGuildSettingByGiteaUserId(event.getRepository().getOwner().getId())
                .switchIfEmpty(Mono.error(new EmptyOptionalException(ALERT_20089)))
                .flatMap(guildSettings -> Mono.<Void>empty()
                        .tap(OnCompleteSignalListenerBuilder.of(
                                () -> log.info("Dialog update request received with content: {}", event)))
                        .then(treeRootService.updateRoot(event))
                        .contextWrite(Context.of(GUILD_ID_MDC_KEY, guildSettings.getGuildId())))
                .then();
    }
}
