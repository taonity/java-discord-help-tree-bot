package discord.tree;

import discord.exception.TreeRootValidationException;
import discord.services.DialogService;
import discord.utils.validation.RootValidator;
import discord4j.core.GatewayDiscordClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class TreeRoot {
    @Getter
    private Node root;

    private final DialogService dialogService;
    private final GatewayDiscordClient gateway;

    @PostConstruct
    private void postConstruct() {
        root = dialogService.readTree();
        root.identifyNodes();
    }

    public void updateRoot() {
        final Node newRoot = dialogService.readTree();
        newRoot.identifyNodes();

        final RootValidator rootValidator = new RootValidator(newRoot, gateway);
        var messageCollector = rootValidator.validateRoot();

        if(messageCollector.isEmpty()) {
            root = newRoot;
        } else {
            throw new TreeRootValidationException(messageCollector.getErrorsAsString());
        }
    }


}
