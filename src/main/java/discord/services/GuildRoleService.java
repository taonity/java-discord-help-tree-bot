package discord.services;

import discord.exception.main.EmptyOptionalException;
import discord.logging.LogMessage;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.RoleCreateSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildRoleService {

    public final static String ROLE_NAME = "helpbot-moderator";

    public void createModeratorRole(Guild guild) {
        final var roleList = guild.getRoles()
                .filter(role -> role.getName().equals(ROLE_NAME))
                .collectList()
                .blockOptional()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20052));

        if(roleList.isEmpty()) {
            guild.createRole(RoleCreateSpec.builder()
                    .name(ROLE_NAME)
                    .build())
                    .subscribe();
        }
    }
}
