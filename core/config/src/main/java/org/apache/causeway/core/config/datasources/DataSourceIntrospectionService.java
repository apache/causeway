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
package org.apache.causeway.core.config.datasources;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.sql.DataSource;

import jakarta.annotation.Priority;
import jakarta.inject.Named;

import org.jspecify.annotations.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.core.config.CausewayModuleCoreConfig;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * For a given <i>Spring</i> context, makes information about configured data-sources available.
 *
 * @apiNote The {@link DataSourceInfo} value type can be extended as needed.
 *
 * @since 2.0 {@index}
 * @see <a href="https://stackoverflow.com/questions/44446597/where-does-the-default-datasource-url-for-h2-come-from-on-spring-boot">stackoverflow.com</a>
 */
@Service
@Named(CausewayModuleCoreConfig.NAMESPACE + "..DataSourceIntrospectionService")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Slf4j
public class DataSourceIntrospectionService {

    @Autowired(required = false)
    private List<DataSource> dataSources;

    public record DataSourceInfo(String jdbcUrl) {

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

        var registeredDataSources = Can.ofCollection(dataSources);

        if(log.isDebugEnabled()) {
            log.debug("about to introspect data-sources: {}",
                    registeredDataSources.map(ds->ds.getClass().getName()));
        }

        return registeredDataSources
                .stream()
                .map(this::dsInfoForDataSource)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Optional<DataSourceInfo> dsInfoForDataSource(
            final @NonNull DataSource dataSource) {

        // make sure we close any connection after having consumed the meta-data
        try(var connection = dataSource.getConnection()){
            return Optional.ofNullable(connection.getMetaData())
                    .map(DataSourceInfo::fromMetaData);
        } catch (SQLException e) {
            log.warn("failed to get metadata from SQL connection using datasource of type {}",
                    dataSource.getClass());
        }
        return Optional.empty();
    }

}
