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
package org.apache.isis.persistence.jdo.datanucleus.test;

import javax.jdo.PersistenceManagerFactory;
import javax.sql.DataSource;

import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.store.rdbms.datasource.dbcp2.BasicDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.commons.internal.debug._Probe;

import lombok.val;

/**
 *  Corresponds to the documents of the 'spring-jdo' module.
 */
@Configuration
@Import({
})
public class ConfigurationExample2 {
    
    @Bean(destroyMethod = "close")
    public DataSource getDataSource() {
        val dataSourceBuilder = DataSourceBuilder.create().type(BasicDataSource.class);
        dataSourceBuilder.driverClassName("org.h2.Driver");
        dataSourceBuilder.url("jdbc:h2:mem:test");
        dataSourceBuilder.username("sa");
        dataSourceBuilder.password("");
        return dataSourceBuilder.build();
    }
  
    @Bean(destroyMethod = "close")
    public PersistenceManagerFactory myPmf(final DataSource dataSource) {
        
        _Probe.errOut("dataSource: %s", dataSource);
        
        val myPmf = new JDOPersistenceManagerFactory();
        myPmf.setConnectionFactory(dataSource);
        myPmf.setNontransactionalRead(true);
        return myPmf;
    }

}
