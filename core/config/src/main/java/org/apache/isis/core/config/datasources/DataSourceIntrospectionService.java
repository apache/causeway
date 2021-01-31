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

import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import bsh.EvalError;

/**
 * For a given <i>Spring</i> context, provides utilities to introspect the list of 
 * configured data-sources.
 *  
 * @since 2.0 {@index}
 */
@Service
@Named("isis.config.DataSourceIntrospectionService")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class DataSourceIntrospectionService {

    @Inject private List<DataSource> dataSources;
    
    @Value
    public static class DataSourceInfo {
        private final String jdbcUrl; 
    }
    
    @SneakyThrows
    public Stream<DataSourceInfo> streamDataSourceInfos() {
        
        log.info("about to introspect data-sources: {}", 
                _NullSafe.stream(dataSources).map(ds->ds.getClass().getName()));
        
        bsh.Capabilities.setAccessibility(true); // allows us to access private fields
        val shell = new bsh.Interpreter();  // construct a new bean-shell interpreter
        
        return _NullSafe.stream(dataSources)
        .map(dataSource->{
            try {
                shell.set("ds", dataSource);
            } catch (EvalError e) { 
                // unexpected
                e.printStackTrace();
                return (String) null;
            }
            val dsClassName = dataSource.getClass().getName();
            if("org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory$EmbeddedDataSourceProxy"
                    .equals(dsClassName)) {
                return (String) Result.of(()->shell.eval("ds.dataSource.url")).nullableOrElse(null);
            }
            if("com.zaxxer.hikari.HikariDataSource"
                    .equals(dsClassName)) {
                return (String) Result.of(()->shell.eval("ds.getJdbcUrl()")).nullableOrElse(null);
            }
            log.warn("don't know how to extract the jdbc url from datasource of type {}; "
                    + "ignoring it as a h2 candidate", 
                    dsClassName);
            return (String) null;
        })
        .filter(_Strings::isNotEmpty)
        .map(DataSourceInfo::new);
    }
    
}
