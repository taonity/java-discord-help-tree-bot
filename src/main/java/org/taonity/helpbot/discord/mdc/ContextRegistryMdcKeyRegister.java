package org.taonity.helpbot.discord.mdc;

import io.micrometer.context.ContextRegistry;
import org.slf4j.MDC;

public class ContextRegistryMdcKeyRegister {

    public static String GUILD_ID_MDC_KEY = "guildId";
    public static String USER_ID_MDC_KEY = "userId";
    public static String COMMAND_NAME_MDC_KEY = "commandName";

    public static void init() {
        ContextRegistry.getInstance().registerThreadLocalAccessor(new MdcAccessor());
        register(GUILD_ID_MDC_KEY);
        register(USER_ID_MDC_KEY);
        register(COMMAND_NAME_MDC_KEY);
    }

    private static void register(String key) {
        ContextRegistry.getInstance()
                .registerThreadLocalAccessor(
                        key, () -> MDC.get(key), value -> MDC.put(key, value), () -> MDC.remove(key));
    }
}
