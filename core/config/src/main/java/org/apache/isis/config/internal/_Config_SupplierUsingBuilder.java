package org.apache.isis.config.internal;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.config.internal._Config.ConfigSupplier;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;

class _Config_SupplierUsingBuilder implements ConfigSupplier {

    final _Lazy<IsisConfiguration> configuration;
    
    _Config_SupplierUsingBuilder(IsisConfigurationBuilder builder) {
        configuration = _Lazy.of(builder::build);
    }

    @Override
    public IsisConfiguration get() {
        return configuration.get();
    }
    
}
