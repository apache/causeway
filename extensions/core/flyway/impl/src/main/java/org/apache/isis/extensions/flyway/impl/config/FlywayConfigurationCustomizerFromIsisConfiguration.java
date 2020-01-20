package org.apache.isis.extensions.flyway.impl.config;

import javax.sql.DataSource;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.Value;
import lombok.val;

@Component
@Order(OrderPrecedence.HIGH)
public class FlywayConfigurationCustomizerFromIsisConfiguration implements FlywayConfigurationCustomizer {

    private final IsisConfiguration isisConfiguration;

    public FlywayConfigurationCustomizerFromIsisConfiguration(
            final IsisConfiguration isisConfiguration) {
        this.isisConfiguration = isisConfiguration;
    }

    @Override
    public void customize(final FluentConfiguration configuration) {
        val params = obtainParams(isisConfiguration);
        val datasource = params.createDatasource();

        configuration.dataSource(datasource);
    }

    /**
     * Searches for JDO connection params.
     *
     * <p>
     *     In the future, could also search for JPA etc.
     * </p>
     */
    private static JdbcConnectionParams obtainParams(final IsisConfiguration isisConfiguration) {

        val javaxJdoOption = isisConfiguration.getPersistence().getJdoDatanucleus().getImpl().getJavax().getJdo().getOption();

        val connectionDriverName = javaxJdoOption.getConnectionDriverName();
        val connectionUrl = javaxJdoOption.getConnectionUrl();
        val connectionUserName = javaxJdoOption.getConnectionUserName();
        val connectionPassword = javaxJdoOption.getConnectionPassword();

        return new JdbcConnectionParams(connectionDriverName, connectionUrl, connectionUserName, connectionPassword);
    }

    @Value
    static class JdbcConnectionParams {
        private final String connectionDriverName;
        private final String connectionUrl;
        private final String connectionUserName;
        private final String connectionPassword;

        public DataSource createDatasource() {
            val dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.driverClassName(connectionDriverName);
            dataSourceBuilder.url(connectionUrl);
            dataSourceBuilder.username(connectionUserName);
            dataSourceBuilder.password(connectionPassword);
            return dataSourceBuilder.build();
        }
    }


}
