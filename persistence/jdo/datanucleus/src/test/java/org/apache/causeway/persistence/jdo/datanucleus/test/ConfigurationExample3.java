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
package org.apache.causeway.persistence.jdo.datanucleus.test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.PersistenceManagerFactory;
import javax.sql.DataSource;

import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.store.rdbms.datasource.dbcp2.BasicDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.Getter;
import lombok.val;

/**
 *  Corresponds to the documents of the 'spring-jdo' module.
 */
@Configuration
@Import({
})
public class ConfigurationExample3 {

    @Bean
    public MetaModelContext getMetaModelContext() {
        return MetaModelContext_forTesting.buildDefault();
    }

    @Bean(destroyMethod = "close")
    public DataSource getDataSource() {
        val dataSourceBuilder = DataSourceBuilder.create().type(BasicDataSource.class);
        dataSourceBuilder.driverClassName("org.h2.Driver");
        dataSourceBuilder.url("jdbc:h2:mem:test");
        dataSourceBuilder.username("sa");
        dataSourceBuilder.password("");
        return dataSourceBuilder.build();
    }

    @Bean(destroyMethod = "close") @Named("myPmf")
    public PersistenceManagerFactory myPmf(final DataSource dataSource) {
        val myPmf = new JDOPersistenceManagerFactory();
        myPmf.setConnectionFactory(dataSource);
        myPmf.setNontransactionalRead(true);
        return myPmf;
    }

    @Bean @Named("myPmfProxy")
    public TransactionAwarePersistenceManagerFactoryProxy myPmfProxy(
            final MetaModelContext metaModelContext,
            final PersistenceManagerFactory myPmf) {
        val myPmfProxy = new TransactionAwarePersistenceManagerFactoryProxy(metaModelContext);
        myPmfProxy.setTargetPersistenceManagerFactory(myPmf);
        myPmfProxy.setAllowCreate(false); // enforce active transactions
        return myPmfProxy;
    }

    @Component
    public static class ExampleDao {

        @Inject
        @Named("myPmfProxy")
        @Getter
        private PersistenceManagerFactory persistenceManagerFactory;

    }

}
