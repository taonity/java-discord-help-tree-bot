package org.taonity.helpbot.discord;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface GuildSettingsRepository extends R2dbcRepository<GuildSettings, String> {
    @Modifying
    @Query("UPDATE guild_settings SET log_channel_id = $2 WHERE guild_id = $1")
    Mono<Void> updateLogChannelId(String guildId, String logChannelId);

    @Modifying
    @Query("UPDATE guild_settings SET help_channel_id = $2 WHERE guild_id = $1")
    Mono<Void> updateHelpChannelId(String guildId, String helpChannelId);

    @Modifying
    @Query("UPDATE guild_settings SET gitea_user_id = $2, gitea_user_alphanumeric = $3 WHERE id = $1")
    Mono<Void> updateGiteaUser(int id, int giteaUserId, String giteaUserAlphanumeric);

    Mono<GuildSettings> findGuildSettingByGuildId(String guildId);

    Mono<GuildSettings> findGuildSettingByGiteaUserId(int giteaUserId);
}
