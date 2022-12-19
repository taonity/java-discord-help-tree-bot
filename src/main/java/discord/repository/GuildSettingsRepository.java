package discord.repository;

import discord.model.GuildSettings;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface GuildSettingsRepository extends CrudRepository<GuildSettings, String> {
    @Modifying
    @Query("update GuildSettings g set g.logChannelId = ?2 where g.id = ?1")
    void updateLogChannelId(String guildId, String logChannelId);

    @Modifying
    @Query("update GuildSettings g set g.helpChannelId = ?2 where g.id = ?1")
    void updateHelpChannelId(String guildId, String helpChannelId);

    @Modifying
    @Query("update GuildSettings g set g.giteaUserId = ?2 where g.id = ?1")
    void updateGiteaUserId(String guildId, int giteaUserId);

    Optional<GuildSettings> findGuildSettingById(String guildId);
    Optional<GuildSettings> findGuildSettingByGiteaUserId(int giteaUserId);
}
