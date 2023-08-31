package discord.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Rollback(false)
@ActiveProfiles("ut")
class AppSettingsRepositoryTest {

    @Autowired
    AppSettingsRepository appSettingsRepository;

    @Value("${test3}")
    String test;

    @Test
    @Disabled
    void findFirst() {
        System.out.println(appSettingsRepository.findOne().get());
    }
}
