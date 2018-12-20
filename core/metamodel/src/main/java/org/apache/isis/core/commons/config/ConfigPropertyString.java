package org.apache.isis.core.commons.config;

import java.util.AbstractMap;
import java.util.Map;

public class ConfigPropertyString extends ConfigPropertyAbstract<String> {
    public ConfigPropertyString(final String key, final String defaultValue) {
        super(key, defaultValue);
    }
    public String from(final IsisConfiguration configuration) {
        return configuration.getString(key, defaultValue);
    }

    @Override
    public Map.Entry<String, String> of(final String value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

}
