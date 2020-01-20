/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.extensions.flyway.impl;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.IsisModuleCoreConfig;

import lombok.Value;
import lombok.val;

@Configuration
@Import({
        IsisModuleCoreConfig.class
})
@ComponentScan
public class IsisModuleExtFlywayImpl {

    @Value
    static class JdbcConnectionParams {
        private final String connectionDriverName;
        private final String connectionUrl;
        private final String connectionUserName;
        private final String connectionPassword;

        public <T extends DataSource> DataSourceBuilder<T> configure(DataSourceBuilder<T> dataSourceBuilder) {
            dataSourceBuilder.driverClassName(connectionDriverName);
            dataSourceBuilder.url(connectionUrl);
            dataSourceBuilder.username(connectionUserName);
            dataSourceBuilder.password(connectionPassword);
            return dataSourceBuilder;
        }
    }

    @Bean
    @FlywayDataSource
    public DataSource getDataSource(final IsisConfiguration isisConfiguration) {

        JdbcConnectionParams params = obtainParams(isisConfiguration);

        val dataSourceBuilder = DataSourceBuilder.create();
        params.configure(dataSourceBuilder);
        return dataSourceBuilder.build();
    }

    /**
     * Searches for JDO connection params.
     *
     * <p>
     *     In the future, could also search for JPA etc.
     * </p>
     */
    private static JdbcConnectionParams obtainParams(IsisConfiguration isisConfiguration) {

        val javaxJdoOption = isisConfiguration.getPersistence().getJdoDatanucleus().getImpl().getJavax().getJdo().getOption();

        val connectionDriverName = javaxJdoOption.getConnectionDriverName();
        val connectionUrl = javaxJdoOption.getConnectionUrl();
        val connectionUserName = javaxJdoOption.getConnectionUserName();
        val connectionPassword = javaxJdoOption.getConnectionPassword();

        return new JdbcConnectionParams(connectionDriverName, connectionUrl, connectionUserName, connectionPassword);
    }

}
