package org.apache.isis.config.property;

import org.apache.isis.config.IsisConfiguration;

public abstract class ConfigPropertyAbstract<T> implements ConfigProperty<T> {
    protected final String key;
    final T defaultValue;
    public ConfigPropertyAbstract(final String key, final T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }
    public abstract T from(final IsisConfiguration configuration);

}
