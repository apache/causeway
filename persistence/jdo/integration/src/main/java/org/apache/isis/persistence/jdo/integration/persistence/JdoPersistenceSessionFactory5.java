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
package org.apache.isis.persistence.jdo.integration.persistence;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.persistence.jdo.spring.integration.LocalPersistenceManagerFactoryBean;

/**
 *
 * Factory for {@link JdoPersistenceSession}.
 *
 */
@Service
@Named("isisJdoDn5.PersistenceSessionFactory5")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("JdoDN5")
@Singleton
public class JdoPersistenceSessionFactory5
implements JdoPersistenceSessionFactory {
    
    @Inject private LocalPersistenceManagerFactoryBean localPmfBean;
    @Inject private MetaModelContext metaModelContext;
    
    @Override
    public JdoPersistenceSession5 createPersistenceSession() {

        Objects.requireNonNull(localPmfBean,
                () -> "PersistenceSessionFactory5 requires initialization. " + this.hashCode());

        return new JdoPersistenceSession5(
                metaModelContext, 
                localPmfBean.getObject());
    }
    

}
