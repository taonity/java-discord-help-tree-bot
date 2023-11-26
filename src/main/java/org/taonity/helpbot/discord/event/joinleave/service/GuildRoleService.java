package org.taonity.helpbot.discord.event.joinleave.service;

import discord4j.core.object.entity.Guild;
import discord4j.core.spec.RoleCreateSpec;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildRoleService {

    public static final String MODERATOR_ROLE_NAME = "questiontree-moderator";

    public Mono<Void> createModeratorRole(Guild guild) {
        return guild.getRoles()
                .filter(role -> role.getName().equals(MODERATOR_ROLE_NAME))
                .collectList()
                .filter(List::isEmpty)
                .then(guild.createRole(RoleCreateSpec.builder()
                                .name(MODERATOR_ROLE_NAME)
                                .build())
                        .then());
    }
}
