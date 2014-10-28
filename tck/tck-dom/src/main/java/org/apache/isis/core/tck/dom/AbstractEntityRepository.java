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

package org.apache.isis.core.tck.dom;

import java.util.List;
import java.util.Map;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.QueryOnly;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;

public abstract class AbstractEntityRepository<T> extends AbstractFactoryAndRepository {

    private final Class<T> entityClass;
    private final String serviceId;
    
    public AbstractEntityRepository(Class<T> entityClass, String serviceId) {
        this.entityClass = entityClass;
        this.serviceId = serviceId;
    }

    @Override
    public final String getId() {
        return serviceId;
    }

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public List<T> list() {
        return allInstances(entityClass);
    }

    @MemberOrder(sequence = "2")
    public T newEntity() {
        final T entity = newTransientInstance(entityClass);
        persist(entity);
        return entity;
    }
    
    @Programmatic
    public T findByNamedQueryFirstOnly(String queryName, Map<String, Object> argumentByParameterName) {
        final Query<T> query = new QueryDefault<T>(entityClass, queryName, argumentByParameterName); 
        return this.firstMatch(query);
    }

    @Programmatic
    public List<T> findByNamedQueryAll(String queryName, Map<String, Object> argumentByParameterName) {
        final Query<T> query = new QueryDefault<T>(entityClass, queryName, argumentByParameterName); 
        return this.allMatches(query);
    }
    
}
