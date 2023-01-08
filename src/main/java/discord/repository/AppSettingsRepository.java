package discord.repository;

import discord.config.AppConfig;
import discord.model.AppSettings;
import discord.model.GuildSettings;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AppSettingsRepository extends CrudRepository<AppSettings, Integer> {
    @Query("select a from AppSettings a")
    Optional<AppSettings> findOne();
}
