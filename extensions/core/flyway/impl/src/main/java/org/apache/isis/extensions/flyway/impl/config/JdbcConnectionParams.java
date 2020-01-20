package org.apache.isis.extensions.flyway.impl.config;

import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.Value;
import lombok.val;

@Value
class JdbcConnectionParams {

    /**
     * Searches for JDO connection params.
     *
     * <p>
     *     In the future, could also search for JPA etc.
     * </p>
     */
    static Optional<JdbcConnectionParams> from(final IsisConfiguration isisConfiguration) {

        IsisConfiguration.Persistence.JdoDatanucleus.Impl.Javax.Jdo.Option javaxJdoOption =
                isisConfiguration.getPersistence().getJdoDatanucleus().getImpl().getJavax().getJdo().getOption();

        val connectionDriverName = javaxJdoOption.getConnectionDriverName();
        val connectionUrl = javaxJdoOption.getConnectionUrl();
        val connectionUserName = javaxJdoOption.getConnectionUserName();
        val connectionPassword = javaxJdoOption.getConnectionPassword();

        if(_Strings.isNullOrEmpty(connectionDriverName) ||
                _Strings.isNullOrEmpty(connectionUrl) ||
                _Strings.isNullOrEmpty(connectionUserName) ||
                _Strings.isNullOrEmpty(connectionPassword)) {
            return Optional.empty();
        }
        return Optional.of(new JdbcConnectionParams(connectionDriverName, connectionUrl, connectionUserName, connectionPassword));
    }

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
