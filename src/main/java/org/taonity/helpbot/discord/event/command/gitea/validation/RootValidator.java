package org.taonity.helpbot.discord.event.command.gitea.validation;

import static java.util.Objects.isNull;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.taonity.helpbot.discord.event.command.tree.model.Node;
import org.taonity.helpbot.discord.localisation.Language;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RootValidator {
    private final Node root;

    private final StringErrorMessageCollector messageCollector = new StringErrorMessageCollector();
    private final GatewayDiscordClient gateway;

    public static Mono<StringErrorMessageCollector> validate(Node root, GatewayDiscordClient gateway) {
        return new RootValidator(root, gateway).validateRoot();
    }

    private Mono<StringErrorMessageCollector> validateRoot() {
        return validateTargetIds()
                .then((Mono.fromRunnable(this::validateLabels)))
                .thenReturn(messageCollector);
    }

    private void validateLabels() {
        var labels = new ArrayList<String>();
        collectLabels(root, labels);
        labels.stream().filter(label -> label.length() >= 100).forEach(messageCollector::addLabelTooBig);
    }

    private Mono<Void> validateTargetIds() {
        var targetIds = new ArrayList<String>();
        collectTargetIds(root, targetIds);
        return Flux.fromIterable(targetIds)
                .map(this::toSnowflake)
                .filter(Objects::nonNull)
                .flatMap(this::validateUserId)
                .then();
    }

    private Snowflake toSnowflake(final String targetId) {
        try {
            return Snowflake.of(targetId);
        } catch (NumberFormatException error) {
            messageCollector.addWrongTarget(targetId);
            return null;
        }
    }

    private Mono<Void> validateUserId(Snowflake targetSnowflake) {
        return gateway.getUserById(targetSnowflake)
                .doOnSuccess(user -> {
                    if (isNull(user)) {
                        messageCollector.addWrongTarget(targetSnowflake.asString());
                    }
                })
                .then();
    }

    private static void collectTargetIds(Node node, List<String> targetIds) {
        String targetId = node.getTargetId();
        if (targetId != null) {
            targetIds.add(targetId);
        }
        if (node.getChildText() != null) {
            for (Node childText : node.getChildText()) {
                collectTargetIds(childText, targetIds);
            }
        }
    }

    private static void collectLabels(Node node, List<String> labels) {
        if (node.getChildText() != null) {
            if (node.getLocalizedText() != null) {
                labels.add(node.getLocalizedText().getTranslatedText(Language.EN));
                labels.add(node.getLocalizedText().getTranslatedText(Language.RU));
            }
            for (Node childText : node.getChildText()) {
                collectLabels(childText, labels);
            }
        }
    }
}
