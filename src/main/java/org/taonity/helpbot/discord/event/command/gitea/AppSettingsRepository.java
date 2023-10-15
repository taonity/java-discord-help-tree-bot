package org.taonity.helpbot.discord.event.command.gitea;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AppSettingsRepository extends CrudRepository<AppSettings, Integer> {
    @Query("select a from AppSettings a")
    Optional<AppSettings> findOne();
}
