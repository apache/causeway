package org.apache.isis.core.commons.config;

public class ConfigPropertyBoolean extends ConfigPropertyAbstract<Boolean> {
    public ConfigPropertyBoolean(final String key, final boolean defaultValue) {
        super(key, defaultValue);
    }
    public Boolean from(final IsisConfiguration configuration) {
        return configuration.getBoolean(key, defaultValue);
    }
}
