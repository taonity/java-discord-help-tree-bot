package org.taonity.helpbot;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.event.command.tree.TreeRootService;

@Component
public class HealthCheckService extends AbstractHealthIndicator {
    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if (TreeRootService.wasCreated()) {
            builder.up();
        } else {
            builder.down();
        }
    }
}
