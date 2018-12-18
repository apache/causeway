package org.apache.isis.core.commons.config;

public class ConfigPropertyEnum<E extends Enum<E>> extends ConfigPropertyAbstract<E> {
    public ConfigPropertyEnum(final String key, final E defaultValue) {
        super(key, defaultValue);
    }
    public E from(final IsisConfiguration configuration) {
        return Enum.valueOf(defaultValue.getDeclaringClass(), configuration.getString(key, defaultValue.name()));
    }
}
