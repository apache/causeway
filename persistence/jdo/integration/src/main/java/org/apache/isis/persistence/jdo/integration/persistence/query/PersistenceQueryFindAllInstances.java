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

import org.apache.isis.applib.query.AllInstancesQuery;
import org.apache.isis.applib.query.QueryRange;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Corresponds to {@link AllInstancesQuery}
 */
@Log4j2
public class PersistenceQueryFindAllInstances extends _PersistenceQueryAbstract  {

    public PersistenceQueryFindAllInstances(
            final ObjectSpecification specification,
            final QueryRange range) {
        super(specification, range);
    }

    @Override
    public Can<ManagedObject> execute(PersistenceQueryContext queryContext) {

        val persistenceQuery = this;
        
        val spec = persistenceQuery.getSpecification();
        val cls = spec.getCorrespondingClass();
        
        val serviceRegistry = spec.getMetaModelContext().getServiceRegistry();
        val isisJdoSupport = isisJdoSupport(serviceRegistry);

        val typesafeQuery = isisJdoSupport.newTypesafeQuery(cls);
        isisJdoSupport.disableMultivaluedFetch(typesafeQuery); // fetch optimization

        if (log.isDebugEnabled()) {
            log.debug("allInstances(): class={}", spec.getFullIdentifier());
        }

        val pojos = isisJdoSupport.executeQuery(cls);
        return loadAdapters(queryContext, pojos);

    }
    
}
