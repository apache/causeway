package org.apache.isis.core.commons.config;

import java.util.Map;

public interface ConfigProperty<T> {
    T from(final IsisConfiguration configuration);

    Map.Entry<String,String> of(final T value);

}
