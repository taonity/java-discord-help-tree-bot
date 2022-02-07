package discord;

public class Configs {
    private String guildId;
    private String channelId;
    private String token;
    private String treePath;

    public Configs(String guildId, String channelId, String treePath, String token) {
        this.guildId = guildId;
        this.channelId = channelId;
        this.treePath = treePath;
        this.token = token;
    }

    public Configs() {
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getTreePath() {
        return treePath;
    }

    public void setTreePath(String treePath) {
        this.treePath = treePath;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
