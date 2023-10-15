package org.taonity.helpbot.discord;

import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChannelRole {
    LOG("log"),
    HELP("help");

    @Getter
    private final String roleName;

    public static ChannelRole valueOfRoleName(String name) {
        for (ChannelRole value : values()) if (value.getRoleName().equalsIgnoreCase(name)) return value;
        throw new IllegalArgumentException();
    }

    public static Optional<ChannelRole> nullableValueOf(String channelRole) {
        try {
            return Optional.of(valueOfRoleName(channelRole));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
