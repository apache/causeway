package org.apache.isis.core.commons.config;

public class ConfigPropertyString extends ConfigPropertyAbstract<String> {
    public ConfigPropertyString(final String key, final String defaultValue) {
        super(key, defaultValue);
    }
    public String from(final IsisConfiguration configuration) {
        return configuration.getString(key, defaultValue);
    }
}
