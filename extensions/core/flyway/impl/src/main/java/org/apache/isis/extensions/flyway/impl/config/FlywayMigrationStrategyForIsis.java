package org.apache.isis.extensions.flyway.impl.config;

import java.util.Optional;
import java.util.Properties;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.val;

@Component
@Order(OrderPrecedence.EARLY)
public class FlywayMigrationStrategyForIsis implements FlywayMigrationStrategy {

    private final IsisConfiguration isisConfiguration;
    private final Optional<JdbcConnectionParams> jdbcConnectionParams;

    public FlywayMigrationStrategyForIsis(IsisConfiguration isisConfiguration) {
        this.isisConfiguration = isisConfiguration;

        org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType x;

        final boolean autoCreate =
                isisConfiguration.getPersistence().getJdoDatanucleus().getImpl().getDatanucleus().getSchema().isAutoCreateAll();
        if(autoCreate) {
            jdbcConnectionParams = Optional.empty();
            return;
        }

        // else
        jdbcConnectionParams = JdbcConnectionParams.from(isisConfiguration);
    }


    @Override
    public void migrate(Flyway flyway) {
        jdbcConnectionParams.ifPresent(
                jdbcConnectionParams1 -> {
                    jdbcConnectionParams1.createDatasource();
                    flyway.migrate();
                }
        );
    }


}
