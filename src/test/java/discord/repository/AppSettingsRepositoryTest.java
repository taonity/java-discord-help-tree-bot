package discord.repository;

import discord.model.AppSettings;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Rollback(false)
@TestPropertySource(locations="classpath:application-test.yml")
class AppSettingsRepositoryTest {

    @Autowired
    AppSettingsRepository appSettingsRepository;

    @Test
    @Disabled
    void findFirst() {
        System.out.println(appSettingsRepository.findOne().get());
    }
}