package discord.services;

import discord.structure.ChannelRole;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageChannelService {
    private final Guild guild;
    private final GuildSettings guildSettings;
    private final GuildSettingsRepository guildSettingsRepository;

    private final Map<ChannelRole, MessageChannel> channelMap = new HashMap<>();

    @PostConstruct
    private void postConstruct() {
        channelMap.put(ChannelRole.LOG, getMessageChannelById(guildSettings.getLogChannelId()));
        channelMap.put(ChannelRole.HELP, getMessageChannelById(guildSettings.getHelpChannelId()));
    }

    private MessageChannel getMessageChannelById(String id) {
        return getMessageChannelById(Snowflake.of(id));
    }

    private MessageChannel getMessageChannelById(Snowflake id) {
        return (MessageChannel) guild.getChannelById(id).block();
    }

    @Transactional
    public void updateChannelById(ChannelRole role, String channelId) {
        channelMap.replace(role, getMessageChannelById(channelId));
        final String guildId = guild.getId().asString();
        switch (role) {
            case HELP:
                guildSettingsRepository.updateHelpChannelId(guildId, channelId);
                break;
            case LOG:
                guildSettingsRepository.updateLogChannelId(guildId, channelId);
                break;
        }
    }

    public MessageChannel getChannel(ChannelRole role) {
        return channelMap.get(role);
    }

}
