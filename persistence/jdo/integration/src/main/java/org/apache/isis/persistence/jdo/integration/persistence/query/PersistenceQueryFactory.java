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
package org.apache.isis.persistence.jdo.integration.persistence.query;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.isis.applib.query.AllInstancesQuery;
import org.apache.isis.applib.query.NamedQuery;
import org.apache.isis.applib.query.Query;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor(staticName = "of") @Log4j2
public class PersistenceQueryFactory implements HasMetaModelContext {

    @Getter(onMethod_ = {@Override})
    private final MetaModelContext metaModelContext;

    /**
     * Converts the {@link org.apache.isis.applib.query.Query} applib representation of a query into the
     * {@link PersistenceQuery} internal representation}.
     */
    public final PersistenceQuery createPersistenceQueryFor(
            final Query<?> query) {
        
        if (log.isDebugEnabled()) {
            log.debug("createPersistenceQueryFor: {}", query.getDescription());
        }
        
        val queryResultTypeSpec = specFor(query);
        val range = QueryRangeModel.of(query.getStart(), query.getCount());
        
        if (query instanceof AllInstancesQuery) {
            return new PersistenceQueryFindAllInstances(
                    queryResultTypeSpec, 
                    range);

        } if (query instanceof NamedQuery) {
            val namedQuery = (NamedQuery<?>) query;
            val queryName = namedQuery.getName();
            val parametersByName = injectServicesInto(namedQuery.getParametersByName());
            
            return new PersistenceQueryFindUsingApplibQueryDefault(
                    queryResultTypeSpec, 
                    queryName, 
                    Collections.unmodifiableMap(parametersByName), 
                    getSpecificationLoader(), 
                    range);
        }
        
        throw _Exceptions.unsupportedOperation("query type %s (%s) not supported by this persistence implementation",
                query.getClass(),
                query.getDescription());
    }

    /**
     * Converts a map of param-pojos keyed by param-name to a map of adapters keyed by the
     * same param-name. 
     * @implNote we do this to ensure queryParameters have injection points resolved (might be redundant) 
     */
    private Map<String, Object> injectServicesInto(
            final @Nullable Map<String, Object> queryParametersByName) {
        
        val injector = getServiceInjector();
        
        // not strictly necessary: creates a copy
        return _Maps.mapValues(queryParametersByName, HashMap::new, paramPojo->
            injector.injectServicesInto(paramPojo)
        );
    }

    private ObjectSpecification specFor(final Query<?> query) {
        return getSpecificationLoader().loadSpecification(query.getResultType());
    }


}
