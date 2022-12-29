package discord.repository;

import discord.model.GuildSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Rollback(false)
class GuildSettingsRepositoryTest {

    @Autowired
    GuildSettingsRepository guildSettingsRepository;

    @Test
    void t() {
        //guildSettingsRepository.save(GuildSettings.builder().id(0).guildId("f").helpChannelId("ff").logChannelId("ff").build());
        //guildSettingsRepository.save(new GuildSettings(12,"dfd","fff", "ffdf", 11));

    }
}