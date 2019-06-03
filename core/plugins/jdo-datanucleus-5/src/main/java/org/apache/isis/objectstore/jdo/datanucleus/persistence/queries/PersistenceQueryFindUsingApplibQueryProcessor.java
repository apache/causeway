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
package org.apache.isis.objectstore.jdo.datanucleus.persistence.queries;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jdo.Query;

import org.apache.isis.applib.services.jdosupport.IsisJdoSupport_v3_2;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.container.query.QueryCardinality;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindUsingApplibQueryDefault;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession5;
import org.apache.isis.objectstore.jdo.datanucleus.metamodel.JdoPropertyUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PersistenceQueryFindUsingApplibQueryProcessor extends PersistenceQueryProcessorAbstract<PersistenceQueryFindUsingApplibQueryDefault> {

    public PersistenceQueryFindUsingApplibQueryProcessor(final PersistenceSession5 persistenceSession) {
        super(persistenceSession);
    }

    @Override
    public List<ObjectAdapter> process(final PersistenceQueryFindUsingApplibQueryDefault persistenceQuery) {
        final String queryName = persistenceQuery.getQueryName();
        final ObjectSpecification objectSpec = persistenceQuery.getSpecification();

        final List<?> results;
        if((objectSpec.getFullIdentifier() + "#pk").equals(queryName)) {
            results = getResultsPk(persistenceQuery);
        } else {
            results = getResults(persistenceQuery);
        }

        return loadAdapters(results);
    }

    // -- HELPER
    
    // special case handling
    private List<?> getResultsPk(final PersistenceQueryFindUsingApplibQueryDefault persistenceQuery) {

        final IsisJdoSupport_v3_2 isisJdoSupport = isisJdoSupport();
        
        final String queryName = persistenceQuery.getQueryName();
        final Map<String, Object> map = unwrap(persistenceQuery.getArgumentsAdaptersByParameterName());
        final ObjectSpecification objectSpec = persistenceQuery.getSpecification();

        final Class<?> cls = objectSpec.getCorrespondingClass();
        if(!JdoPropertyUtils.hasPrimaryKeyProperty(objectSpec)) {
            throw new UnsupportedOperationException("cannot search by primary key for DataStore-assigned entities");
        }
        final OneToOneAssociation pkOtoa = JdoPropertyUtils.getPrimaryKeyPropertyFor(objectSpec);
        final String pkOtoaId = pkOtoa.getId();
        final String filter = pkOtoaId + "==" + map.get(pkOtoaId);
        
        /* FIXME[ISIS-2020] as of Oct. 2018: likely not working on FederatedDataStore
         * see PersistenceQueryFindAllInstancesProcessor for workaround using type-safe query instead
         */
        final Query<?> jdoQuery = persistenceSession.newJdoQuery(cls, filter);
        isisJdoSupport.disableMultivaluedFetch(jdoQuery); // fetch optimization
        
        if (log.isDebugEnabled()) {
            log.debug("{} # {} ( {} )", cls.getName(), queryName, filter);
        }

        try {
            final List<?> results = (List<?>) jdoQuery.execute();
            return _Lists.newArrayList(results);
        } finally {
            jdoQuery.closeAll();
        }
    }

    private List<?> getResults(final PersistenceQueryFindUsingApplibQueryDefault persistenceQuery) {

        final IsisJdoSupport_v3_2 isisJdoSupport = isisJdoSupport();
        
        final String queryName = persistenceQuery.getQueryName();
        final Map<String, Object> argumentsByParameterName = unwrap(
                persistenceQuery.getArgumentsAdaptersByParameterName());
        final QueryCardinality cardinality = persistenceQuery.getCardinality();
        final ObjectSpecification objectSpec = persistenceQuery.getSpecification();

        final Class<?> cls = objectSpec.getCorrespondingClass();
        
        /* FIXME[ISIS-2020] as of Oct. 2018: likely not working on FederatedDataStore
         * see PersistenceQueryFindAllInstancesProcessor for workaround using type-safe query instead 
         */
        final Query<?> jdoQuery = persistenceSession.newJdoNamedQuery(cls, queryName); 
        isisJdoSupport.disableMultivaluedFetch(jdoQuery);
        
        if(persistenceQuery.hasRange()) {
            jdoQuery.setRange(persistenceQuery.getStart(), persistenceQuery.getEnd());
        }

        if (log.isDebugEnabled()) {
            log.debug("{} # {} ( {} )", cls.getName(), queryName, argumentsByParameterName);
        }

        try {
            final List<?> results = (List<?>) jdoQuery.executeWithMap(argumentsByParameterName);
            if(results == null) {
                return Collections.emptyList();
            }
            final List<?> resultsToReturn =
                    cardinality == QueryCardinality.MULTIPLE
                    ? results
                            : firstIfAnyOf(results);
            return _Lists.newArrayList(resultsToReturn);
        } finally {
            jdoQuery.closeAll();
        }
    }

    private List<?> firstIfAnyOf(final List<?> results) {
        return results.isEmpty()
                ? Collections.emptyList()
                        : results.subList(0, 1);
    }

    private static Map<String, Object> unwrap(final Map<String, ObjectAdapter> argumentAdaptersByParameterName) {
        final Map<String, Object> argumentsByParameterName = _Maps.newHashMap();
        for (final String parameterName : argumentAdaptersByParameterName.keySet()) {
            final ObjectAdapter argumentAdapter = argumentAdaptersByParameterName.get(parameterName);
            final Object argument = ObjectAdapter.Util.unwrapPojo(argumentAdapter);
            argumentsByParameterName.put(parameterName, argument);
        }
        return argumentsByParameterName;
    }

}

