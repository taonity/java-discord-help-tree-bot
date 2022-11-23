package discord.utils.validation;

import discord.localisation.Language;
import discord.tree.Node;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.rest.http.client.ClientException;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class RootValidator {
    private final Node root;

    private final StringErrorMessageCollector messageCollector = new StringErrorMessageCollector();
    private final GatewayDiscordClient gateway;

    public StringErrorMessageCollector validateRoot() {
        validateLabels();
        validateLabels();
        return messageCollector;
    }

    private void validateLabels() {
        var labels = new ArrayList<String>();
        collectLabels(root, labels);
        labels.stream()
                .filter(label -> label.length() >= 100)
                .forEach(messageCollector::addLabelTooBig);
    }

    private void validateTargetIds() {
        var targetIds = new ArrayList<String>();
        collectTargetIds(root, targetIds);
        targetIds.stream()
                .map(this::toSnowflake)
                .filter(Objects::nonNull)
                .forEach(this::toUserId);
    }

    private Snowflake toSnowflake(final String targetId) {
        try {
            return Snowflake.of(targetId);
        } catch (NumberFormatException error) {
            messageCollector.addWrongTarget(targetId);
            return null;
        }
    }

    private void toUserId(Snowflake targetSnowflake) {
        try {
            gateway.getUserById(targetSnowflake).block();
        } catch (ClientException error) {
            messageCollector.addWrongTarget(targetSnowflake.asString());
        }
    }

    private static void collectTargetIds(Node node, List<String> targetIds) {
        String targetId = node.getTargetId();
        if(targetId != null) {
            targetIds.add(targetId);
        }
        if(node.getChildText() != null) {
            for(Node childText: node.getChildText()) {
                collectTargetIds(childText, targetIds);
            }
        }
    }

    private static void collectLabels(Node node, List<String> labels) {
        if(node.getChildText() != null) {
            if(node.getLocalizedText() != null) {
                labels.add(node.getLocalizedText().getTranslatedText(Language.EN));
                labels.add(node.getLocalizedText().getTranslatedText(Language.RU));
            }
            for(Node childText: node.getChildText()) {
                collectLabels(childText, labels);
            }
        }
    }

}
