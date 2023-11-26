package org.taonity.helpbot.discord.mdc;

import io.micrometer.context.ThreadLocalAccessor;
import java.util.Map;
import org.slf4j.MDC;

public class MdcAccessor implements ThreadLocalAccessor<Map<String, String>> {

    static final String KEY = "mdc";

    @Override
    public Object key() {
        return KEY;
    }

    @Override
    public Map<String, String> getValue() {
        return MDC.getCopyOfContextMap();
    }

    @Override
    public void setValue(Map<String, String> value) {
        MDC.setContextMap(value);
    }

    @Override
    public void reset() {
        MDC.clear();
    }
}
