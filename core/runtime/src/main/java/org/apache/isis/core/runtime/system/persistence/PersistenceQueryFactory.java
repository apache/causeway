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
package org.apache.isis.core.runtime.system.persistence;

import java.util.Map;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByPattern;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByTitle;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindByPattern;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindUsingApplibQuerySerializable;

public class PersistenceQueryFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceQueryFactory.class);

    private final SpecificationLoader specificationLoader;
    private final AdapterManager adapterManager;

    PersistenceQueryFactory(
            final AdapterManager adapterManager,
            final SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
        this.adapterManager = adapterManager;
    }

    /**
     * Converts the {@link org.apache.isis.applib.query.Query applib representation of a query} into the
     * {@link PersistenceQuery NOF-internal representation}.
     */
    final PersistenceQuery createPersistenceQueryFor(final Query<?> query, final QueryCardinality cardinality) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createPersistenceQueryFor: {}", query.getDescription());
        }
        final ObjectSpecification noSpec = specFor(query);
        if (query instanceof QueryFindAllInstances) {
            final QueryFindAllInstances<?> queryFindAllInstances = (QueryFindAllInstances<?>) query;
            return new PersistenceQueryFindAllInstances(noSpec, specificationLoader, queryFindAllInstances.getStart(), queryFindAllInstances.getCount());
        }
        if (query instanceof QueryFindByTitle) {
            final QueryFindByTitle<?> queryByTitle = (QueryFindByTitle<?>) query;
            final String title = queryByTitle.getTitle();
            return new PersistenceQueryFindByTitle(noSpec, title, specificationLoader, queryByTitle.getStart(), queryByTitle.getCount());
        }
        if (query instanceof QueryFindByPattern) {
            final QueryFindByPattern<?> queryByPattern = (QueryFindByPattern<?>) query;
            final Object pattern = queryByPattern.getPattern();
            final ObjectAdapter patternAdapter = adapterManager.adapterFor(pattern);
            return new PersistenceQueryFindByPattern(noSpec, patternAdapter, specificationLoader, queryByPattern.getStart(), queryByPattern.getCount());
        }
        if (query instanceof QueryDefault) {
            final QueryDefault<?> queryDefault = (QueryDefault<?>) query;
            final String queryName = queryDefault.getQueryName();
            final Map<String, ObjectAdapter> argumentsAdaptersByParameterName = wrap(queryDefault.getArgumentsByParameterName());
            return new PersistenceQueryFindUsingApplibQueryDefault(noSpec, queryName, argumentsAdaptersByParameterName, cardinality,
                    specificationLoader, queryDefault.getStart(), queryDefault.getCount());
        }
        // fallback; generic serializable applib query.
        return new PersistenceQueryFindUsingApplibQuerySerializable(noSpec, query, cardinality, specificationLoader);
    }

    /**
     * Converts a map of pojos keyed by string to a map of adapters keyed by the
     * same strings.
     */
    private Map<String, ObjectAdapter> wrap(final Map<String, Object> argumentsByParameterName) {
        final Map<String, ObjectAdapter> argumentsAdaptersByParameterName = Maps.newHashMap();
        for (final Map.Entry<String, Object> entry : argumentsByParameterName.entrySet()) {
            final String parameterName = entry.getKey();
            final Object argument = argumentsByParameterName.get(parameterName);
            final ObjectAdapter argumentAdapter = argument != null ? adapterManager.adapterFor(argument) : null;
            argumentsAdaptersByParameterName.put(parameterName, argumentAdapter);
        }
        return argumentsAdaptersByParameterName;
    }

    private ObjectSpecification specFor(final Query<?> query) {
        return specificationLoader.loadSpecification(query.getResultType());
    }


}
