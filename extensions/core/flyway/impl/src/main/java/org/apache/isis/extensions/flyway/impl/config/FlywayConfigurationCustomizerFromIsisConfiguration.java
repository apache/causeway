package org.apache.isis.extensions.flyway.impl.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.IsisConfiguration.Persistence.JdoDatanucleus.Impl.Javax.Jdo;

import lombok.Value;
import lombok.val;

@Component
@Order(OrderPrecedence.EARLY)
public class FlywayConfigurationCustomizerFromIsisConfiguration implements FlywayConfigurationCustomizer {

    private final IsisConfiguration isisConfiguration;

    public FlywayConfigurationCustomizerFromIsisConfiguration(
            final IsisConfiguration isisConfiguration) {
        this.isisConfiguration = isisConfiguration;
    }

    @Override
    public void customize(final FluentConfiguration configuration) {

        final boolean autoCreate =
                isisConfiguration.getPersistence().getJdoDatanucleus().getImpl().getDatanucleus().getSchema().isAutoCreateAll();
        if (autoCreate) {
            Properties props = new Properties();
            props.setProperty("spring.flyway.enabled", "false");
            configuration.configuration(props);
            return;
        }

//        // else
//        JdbcConnectionParams.from(isisConfiguration)
//                .map(JdbcConnectionParams::createDatasource)
//                .ifPresent(configuration::dataSource);
    }
}
