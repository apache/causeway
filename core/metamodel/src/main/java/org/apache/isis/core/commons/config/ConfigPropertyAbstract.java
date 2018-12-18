package org.apache.isis.core.commons.config;

public abstract class ConfigPropertyAbstract<T> implements ConfigProperty<T> {
    final String key;
    final T defaultValue;
    public ConfigPropertyAbstract(final String key, final T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }
    public abstract T from(final IsisConfiguration configuration);
}
