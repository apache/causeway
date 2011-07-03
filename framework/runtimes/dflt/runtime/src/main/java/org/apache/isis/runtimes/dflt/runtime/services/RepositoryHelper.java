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

package org.apache.isis.runtimes.dflt.runtime.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceQuery;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

public final class RepositoryHelper {

    private RepositoryHelper() {
    }

    public static Object[] allInstances(final Class<?> cls) {
        return allInstances(getSpecificationLoader().loadSpecification(cls), cls);
    }

    public static <T> Object[] allInstances(final ObjectSpecification spec, final Class<T> cls) {
        final QueryFindAllInstances<T> query = new QueryFindAllInstances<T>(spec.getFullIdentifier());
        final ObjectAdapter instances = getPersistenceSession().findInstances(query, QueryCardinality.MULTIPLE);
        final Object[] array = convertToArray(instances, cls);
        return array;
    }

    public static List<Object> findByPersistenceQuery(final PersistenceQuery persistenceQuery, final Class<?> cls) {
        final ObjectAdapter instances = getPersistenceSession().findInstances(persistenceQuery);
        return convertToList(instances, cls);
    }

    public static List<Object> findByTitle(final Class<?> type, final String title) {
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(type);
        return findByTitle(spec, type, title);
    }

    public static List<Object> findByTitle(final ObjectSpecification spec, final Class<?> cls, final String title) {
        final PersistenceQuery criteria = new PersistenceQueryFindByTitle(spec, title);
        return findByPersistenceQuery(criteria, cls);
    }

    public static boolean hasInstances(final Class<?> type) {
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(type);
        return hasInstances(spec);
    }

    public static boolean hasInstances(final ObjectSpecification spec) {
        return getPersistenceSession().hasInstances(spec);
    }

    // /////////////////////////////////////////////////////////////////////
    // Helpers
    // /////////////////////////////////////////////////////////////////////

    private static List<Object> convertToList(final ObjectAdapter instances, final Class<?> cls) {
        final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(instances);
        final List<Object> list = new ArrayList<Object>();
        for (final ObjectAdapter adapter : facet.iterable(instances)) {
            list.add(adapter.getObject());
        }
        return list;
    }

    private static Object[] convertToArray(final ObjectAdapter instances, final Class<?> cls) {
        return convertToList(instances, cls).toArray();
    }

    // /////////////////////////////////////////////////////////////////////
    // Context lookup
    // /////////////////////////////////////////////////////////////////////

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private static SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}
