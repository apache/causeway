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
package org.apache.isis.core.config.datasources;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Priority;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * For a given <i>Spring</i> context, makes information about configured data-sources available.
 *
 * @apiNote The {@link DataSourceInfo} value type can be extended as needed.
 *
 * @since 2.0 {@index}
 * @see <a href="https://stackoverflow.com/questions/44446597/where-does-the-default-datasource-url-for-h2-come-from-on-spring-boot">stackoverflow.com</a>
 */
@Service
@Named("isis.config.DataSourceIntrospectionService")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class DataSourceIntrospectionService {

    @Autowired(required = false)
    private List<DataSource> dataSources;

    @Value
    public static class DataSourceInfo {
        private final String jdbcUrl;

        @SneakyThrows
        private static DataSourceInfo fromMetaData(final @NonNull DatabaseMetaData databaseMetaData) {
            return new DataSourceInfo(databaseMetaData.getURL());
        }
    }

    public Can<DataSourceInfo> getDataSourceInfos() {
        return dataSourceInfos.get();
    }

    // -- HELPER

    private final _Lazy<Can<DataSourceInfo>> dataSourceInfos =
            _Lazy.threadSafe(()->Can.ofStream(streamDataSourceInfos()));

    private Stream<DataSourceInfo> streamDataSourceInfos() {

        val registeredDataSources = Can.ofCollection(dataSources);

        log.debug("about to introspect data-sources: {}",
                ()->registeredDataSources.map(ds->ds.getClass().getName()));

        return registeredDataSources
                .stream()
                .map(this::dsInfoForDataSource)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<DataSourceInfo> dsInfoForDataSource(
            final @NonNull DataSource dataSource) {

        // make sure we close any connection after having consumed the meta-data
        try(val connection = dataSource.getConnection()){
            return Optional.ofNullable(connection.getMetaData())
                    .map(DataSourceInfo::fromMetaData);
        } catch (SQLException e) {
            log.warn("failed to get metadata from SQL connection using datasource of type {}",
                    dataSource.getClass());
        }
        return Optional.empty();
    }


}
