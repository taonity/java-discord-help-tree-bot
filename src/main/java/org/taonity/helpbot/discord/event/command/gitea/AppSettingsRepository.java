package org.taonity.helpbot.discord.event.command.gitea;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface AppSettingsRepository extends R2dbcRepository<AppSettings, Integer> {
    @Query("SELECT * FROM app_settings a")
    Mono<AppSettings> findOne();
}
