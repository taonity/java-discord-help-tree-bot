package org.taonity.helpbot.discord.event.joinleave.service;

import discord4j.core.object.entity.Guild;
import discord4j.core.spec.RoleCreateSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildRoleService {

    public static final String MODERATOR_ROLE_NAME = "questiontree-moderator";

    public void createModeratorRole(Guild guild) {
        final var roleList = guild.getRoles()
                .filter(role -> role.getName().equals(MODERATOR_ROLE_NAME))
                .collectList()
                .blockOptional()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20052));

        if (roleList.isEmpty()) {
            guild.createRole(RoleCreateSpec.builder().name(MODERATOR_ROLE_NAME).build())
                    .subscribe();
        }
    }
}
