package org.apache.isis.core.commons.config;

public interface ConfigProperty<T> {
    T from(final IsisConfiguration configuration);
}
