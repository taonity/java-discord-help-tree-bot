package discord.repository;

import discord.model.GuildSettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface GuildSettingsRepository extends CrudRepository<GuildSettings, String> {
    @Modifying
    @Query("update GuildSettings g set g.logChannelId = ?2 where g.guildId = ?1")
    void updateLogChannelId(String guildId, String logChannelId);

    @Modifying
    @Query("update GuildSettings g set g.helpChannelId = ?2 where g.guildId = ?1")
    void updateHelpChannelId(String guildId, String helpChannelId);

    @Modifying
    @Query("update GuildSettings g set g.giteaUserId = ?2, g.giteaUserAlphanumeric = ?3 where g.id = ?1")
    void updateGiteaUser(int id, int giteaUserId, String giteaUserAlphanumeric);

    Optional<GuildSettings> findGuildSettingByGuildId(String guildId);

    Optional<GuildSettings> findGuildSettingByGiteaUserId(int giteaUserId);
}
