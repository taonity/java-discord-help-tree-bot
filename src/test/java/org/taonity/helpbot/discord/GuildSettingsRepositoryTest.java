package org.taonity.helpbot.discord;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Rollback(false)
class GuildSettingsRepositoryTest {

    @Autowired
    GuildSettingsRepository guildSettingsRepository;

    @Test
    @Disabled
    void save() {
        final var guildSettings = GuildSettings.builder()
                .id(100)
                .guildId("f")
                .helpChannelId("ff")
                .logChannelId("ff")
                .build();

        guildSettingsRepository.save(guildSettings);
    }
}
