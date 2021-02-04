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

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;

import lombok.Value;
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
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class DataSourceIntrospectionService {

    @Autowired(required = false)
    private List<DataSource> dataSources;
    
    @Value
    public static class DataSourceInfo {
        private final String jdbcUrl; 
    }
    
    public Stream<DataSourceInfo> streamDataSourceInfos() {
        
        log.debug("about to introspect data-sources: {}", 
                ()->Can.ofCollection(dataSources).map(ds->ds.getClass().getName()));
        
        return _NullSafe.stream(dataSources)
        .map(dataSource->{
            try {
                return dataSource.getConnection().getMetaData().getURL();
            } catch (SQLException e) { 
                // unexpected
                e.printStackTrace();
                return (String) null;
            }
        })
        .filter(_Strings::isNotEmpty)
        .map(DataSourceInfo::new);
    }
    
}
