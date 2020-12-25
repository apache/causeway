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
import java.util.List;
import java.util.Map;

import org.apache.isis.applib.query.Query;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.persistence.jdo.integration.metamodel.JdoPropertyUtils;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Corresponds to an object-store specific implementation of {@link Query}.
 */
@Log4j2
public class PersistenceQueryFindUsingApplibQueryDefault extends _PersistenceQueryAbstract {

    @Getter private final String queryName;
    @Getter private final Map<String, Object> queryParametersByName;

    public PersistenceQueryFindUsingApplibQueryDefault(
            final ObjectSpecification specification,
            final String queryName,
            final Map<String, Object> queryParametersByName,
            final QueryRangeModel range) {
        super(specification, range);
        this.queryName = queryName;
        this.queryParametersByName = queryParametersByName;
    }

    @Override
    public Can<ManagedObject> execute(PersistenceQueryContext queryContext) {

        val persistenceQuery = this;
        
        val queryName = persistenceQuery.getQueryName();
        val objectSpec = persistenceQuery.getSpecification();

        val resultList = (objectSpec.getFullIdentifier() + "#pk").equals(queryName)
                ? getResultsPk(queryContext, persistenceQuery)
                : getResults(queryContext, persistenceQuery);

        return loadAdapters(queryContext, resultList);
    }

    // -- HELPER

    // special case handling
    private List<?> getResultsPk(
            final PersistenceQueryContext queryContext,
            final PersistenceQueryFindUsingApplibQueryDefault persistenceQuery) {

        val queryName = persistenceQuery.getQueryName();
        val queryParametersByName = persistenceQuery.getQueryParametersByName();
        val spec = persistenceQuery.getSpecification();
        
        val isisJdoSupport = isisJdoSupport(spec.getMetaModelContext().getServiceRegistry());

        val cls = spec.getCorrespondingClass();
        if(!JdoPropertyUtils.hasPrimaryKeyProperty(spec)) {
            throw new UnsupportedOperationException("cannot search by primary key for DataStore-assigned entities");
        }
        final OneToOneAssociation pkOtoa = JdoPropertyUtils.getPrimaryKeyPropertyFor(spec);
        final String pkOtoaId = pkOtoa.getId();
        final String filter = pkOtoaId + "==" + queryParametersByName.get(pkOtoaId);

        /* XXX[ISIS-2020] as of Oct. 2018: likely not working on FederatedDataStore
         * see PersistenceQueryFindAllInstancesProcessor for workaround using type-safe query instead
         */
        //final Query<?> 
        val jdoQuery = queryContext.newJdoQuery(cls, filter);
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

    private List<?> getResults(
            final PersistenceQueryContext queryContext,
            final PersistenceQueryFindUsingApplibQueryDefault persistenceQuery) {

        val queryName = persistenceQuery.getQueryName();
        val queryParametersByName = persistenceQuery.getQueryParametersByName();
        val spec = persistenceQuery.getSpecification();
        val cls = spec.getCorrespondingClass();
        
        val serviceRegistry = spec.getMetaModelContext().getServiceRegistry();
        val isisJdoSupport = isisJdoSupport(serviceRegistry);

        /* XXX[ISIS-2020] as of Oct. 2018: likely not working on FederatedDataStore
         * see PersistenceQueryFindAllInstancesProcessor for workaround using type-safe query instead 
         */
        //final Query<?> 
        val jdoQuery = queryContext.newJdoNamedQuery(cls, queryName); 
        isisJdoSupport.disableMultivaluedFetch(jdoQuery);

        val queryRangeModel = persistenceQuery.getQueryRangeModel();
        
        if(queryRangeModel.hasRange()) {
            jdoQuery.setRange(queryRangeModel.getStart(), queryRangeModel.getEnd());
        }

        if (log.isDebugEnabled()) {
            log.debug("{} # {} ( {} )", cls.getName(), queryName, queryParametersByName);
        }
        
        try {
            val resultList = (List<?>) jdoQuery.executeWithMap(queryParametersByName);
            if(resultList == null
                    || resultList.isEmpty()) {
                return Collections.emptyList();
            }
            
            if(queryRangeModel.hasRange()) {
                _Assert.assertTrue(resultList.size()<=queryRangeModel.getCount());
            }
            
            // puzzler: needs a defensive copy, not sure why
            return _Lists.newArrayList(resultList);
        } finally {
            jdoQuery.closeAll();
        }
    }
}
