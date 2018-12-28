package org.apache.isis.config.property;

import java.util.Map;

import org.apache.isis.config.IsisConfiguration;

public interface ConfigProperty<T> {
    T from(final IsisConfiguration configuration);

    Map.Entry<String,String> of(final T value);

}
