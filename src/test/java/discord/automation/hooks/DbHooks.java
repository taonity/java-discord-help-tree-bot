package discord.automation.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@RequiredArgsConstructor
public class DbHooks {

    private final DataSource dataSource;

    @Before(value = "@DbCleanUp")
    @After(value = "@DbCleanUp")
    public void clearAddedData() {
        ClassPathResource resource = new ClassPathResource("automation/sql/cleanUp.sql");
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator(resource);
        resourceDatabasePopulator.execute(dataSource);
    }
}
