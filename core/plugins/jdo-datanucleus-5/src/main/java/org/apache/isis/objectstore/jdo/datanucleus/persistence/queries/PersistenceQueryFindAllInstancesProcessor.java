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

import java.util.List;

import javax.jdo.JDOQLTypedQuery;

import org.apache.isis.applib.services.jdosupport.IsisJdoSupport_v3_2;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession5;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PersistenceQueryFindAllInstancesProcessor extends PersistenceQueryProcessorAbstract<PersistenceQueryFindAllInstances> {

    public PersistenceQueryFindAllInstancesProcessor(final PersistenceSession5 persistenceSession) {
        super(persistenceSession);
    }

    @Override
    public List<ObjectAdapter> process(final PersistenceQueryFindAllInstances persistenceQuery) {

        final IsisJdoSupport_v3_2 isisJdoSupport = isisJdoSupport();
        
        final ObjectSpecification specification = persistenceQuery.getSpecification();
        final Class<?> cls = specification.getCorrespondingClass();

        JDOQLTypedQuery<?> typesafeQuery = isisJdoSupport.newTypesafeQuery(cls);
        isisJdoSupport.disableMultivaluedFetch(typesafeQuery); // fetch optimization

        if (log.isDebugEnabled()) {
            log.debug("allInstances(): class={}", specification.getFullIdentifier());
        }
        
        final List<?> pojos = isisJdoSupport.executeQuery(cls);
        return loadAdapters(pojos);

    }
    
}
