package org.apache.isis.core.commons.config;

import java.util.AbstractMap;
import java.util.Map;

public class ConfigPropertyBoolean extends ConfigPropertyAbstract<Boolean> {
    public ConfigPropertyBoolean(final String key, final boolean defaultValue) {
        super(key, defaultValue);
    }
    public Boolean from(final IsisConfiguration configuration) {
        return configuration.getBoolean(key, defaultValue);
    }

    @Override
    public Map.Entry<String, String> of(final Boolean value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value.toString());
    }

}
