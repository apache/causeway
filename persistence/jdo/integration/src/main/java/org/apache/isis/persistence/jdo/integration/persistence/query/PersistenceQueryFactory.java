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

import java.util.Map;
import java.util.function.Function;

import org.apache.isis.applib.query.AllInstancesQuery;
import org.apache.isis.applib.query.NamedQuery;
import org.apache.isis.applib.query.Query;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor(staticName = "of") @Log4j2
public class PersistenceQueryFactory implements HasMetaModelContext {

    @Getter(onMethod_ = {@Override})
    private final MetaModelContext metaModelContext;

    /**
     * Converts the {@link org.apache.isis.applib.query.Query applib representation of a query} into the
     * {@link PersistenceQuery NOF-internal representation}.
     */
    public final PersistenceQuery createPersistenceQueryFor(
            final Function<Object, ManagedObject> adapterProvider,
            final Query<?> query, 
            final QueryCardinality cardinality) {
        
        if (log.isDebugEnabled()) {
            log.debug("createPersistenceQueryFor: {}", query.getDescription());
        }
        final ObjectSpecification noSpec = specFor(query);
        if (query instanceof AllInstancesQuery) {
            final AllInstancesQuery<?> queryFindAllInstances = (AllInstancesQuery<?>) query;
            return new PersistenceQueryFindAllInstances(noSpec, queryFindAllInstances.getStart(), queryFindAllInstances.getCount());

        } if (query instanceof NamedQuery) {
            final NamedQuery<?> queryDefault = (NamedQuery<?>) query;
            final String queryName = queryDefault.getName();
            final Map<String, ManagedObject> parametersByName = 
                    wrap(adapterProvider, queryDefault.getParametersByName());
            return new PersistenceQueryFindUsingApplibQueryDefault(noSpec, queryName, parametersByName, cardinality,
                    getSpecificationLoader(), queryDefault.getStart(), queryDefault.getCount());
        }
        throw _Exceptions.unsupportedOperation("query type %s not supported by this persistence implementation",
                query.getClass());
    }

    /**
     * Converts a map of pojos keyed by string to a map of adapters keyed by the
     * same strings.
     */
    private Map<String, ManagedObject> wrap(
            final Function<Object, ManagedObject> adapterProvider,
            final Map<String, Object> argumentsByParameterName) {
        
        final Map<String, ManagedObject> argumentsAdaptersByParameterName = _Maps.newHashMap();
        for (final Map.Entry<String, Object> entry : argumentsByParameterName.entrySet()) {
            final String parameterName = entry.getKey();
            final Object argument = argumentsByParameterName.get(parameterName);
            final ManagedObject argumentAdapter = argument != null ? adapterProvider.apply(argument) : null;
            argumentsAdaptersByParameterName.put(parameterName, argumentAdapter);
        }
        return argumentsAdaptersByParameterName;
    }

    private ObjectSpecification specFor(final Query<?> query) {
        return getSpecificationLoader().loadSpecification(query.getResultType());
    }


}
