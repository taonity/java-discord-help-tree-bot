package discord.automation.runners;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

import discord.automation.config.GiteaApiTestService;
import discord.automation.config.GiteaUserTestService;
import discord.config.PropertyConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("automation/features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "discord.automation")
// Is crucial for Junit 4 to 5 migration, for testcontainers
@CucumberContextConfiguration
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
// DB configs
@EntityScan("discord.model")
@EnableJpaRepositories(basePackages = {"discord.repository"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
// Gitea configs
@ContextConfiguration(
        initializers = {ConfigDataApplicationContextInitializer.class},
        classes = {PropertyConfig.class, GiteaApiTestService.class, GiteaUserTestService.class})
public class CucumberRunnerIT extends AbstractContainerRunner {}
