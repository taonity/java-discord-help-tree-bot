package discord.services;

import discord.config.PropertyConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class, classes = {GiteaApiService.class, PropertyConfig.class})
@RunWith(SpringRunner.class)
public class GiteaApiServiceTest {

    @Autowired
    GiteaApiService giteaApiService;

    @Value("${app-reference.protocol}://${app-reference.address}:${server.port}")
    String serverPath;

    @Test
    public void testGetUsers() throws InterruptedException {
        String username = "test_user1211111";
        String email = "d@d.d31211111";
        String repo = "test_repo1211111";
        String filepath = "test_file11.json";
        //System.out.println(giteaApiService.createUser(new CreateUserOption(username, "d", email)));
        //giteaApiService.createRepository(username, new CreateRepoOption(repo));
        //giteaApiService.createFile(username, repo, filepath, new CreateFileOption("test_text"));
        // TODO: falls without delay
        Thread.sleep(500);
        /*System.out.println(giteaApiService.getFile(username, repo, filepath, GiteaApiService.BRANCH_NAME));
        System.out.println(giteaApiService.getFile(username, repo, filepath, GiteaApiService.BRANCH_NAME));
        System.out.println(giteaApiService.getFile(username, repo, filepath, GiteaApiService.BRANCH_NAME));
        System.out.println(giteaApiService.getFile(username, repo, filepath, GiteaApiService.BRANCH_NAME));*/
    }

    @Test
    public void testIt() throws InterruptedException {
        String username = "test_user";
        String email = "d@d.d312111111";
        String repo = "test_repo";
        String filepath = "test_file11.json";
        //System.out.println(giteaApiService.createUser(new GiteaNewUser(username, "d", email)));
        //System.out.println(giteaApiService.createRepository(username, new GiteaNewRepo(repo)));
        //System.out.println(giteaApiService.createFile(username, repo, filepath, new GiteaNewFile("test_text")));
        //System.out.println(giteaApiService.getFile(username, repo, filepath, GiteaApiService.BRANCH_NAME).orElse(null));
        //System.out.println(giteaApiService.editUser(new GiteaEditUser(username, "d123456")));
        //System.out.println(giteaApiService.deleteRepo("test_user1211111","test_repo1211111"));
        //System.out.println(giteaApiService.deleteUser("test_user1211111"));
        //System.out.println(giteaApiService.getReposByUid(3));
        //System.out.println(giteaApiService.getCommits(username, repo, 1));
        //giteaApiService.createFile(username, repo, filepath, new CreateFileOption("test_text"));
        //giteaApiService.createHook(username, repo);


    }

    @Test
    public void pass() {

    }
}