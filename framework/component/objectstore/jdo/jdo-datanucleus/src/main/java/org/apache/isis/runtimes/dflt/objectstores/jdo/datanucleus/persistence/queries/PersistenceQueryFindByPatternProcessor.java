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
package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.queries;

import java.util.List;

import javax.jdo.PersistenceManager;

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.FrameworkSynchronizer;
import org.apache.isis.runtimes.dflt.runtime.persistence.query.PersistenceQueryFindByPattern;

public class PersistenceQueryFindByPatternProcessor extends
        PersistenceQueryProcessorAbstract<PersistenceQueryFindByPattern> {

    public PersistenceQueryFindByPatternProcessor(
            final PersistenceManager persistenceManager, final FrameworkSynchronizer frameworkSynchronizer) {
        super(persistenceManager, frameworkSynchronizer);
    }

    public List<ObjectAdapter> process(
            final PersistenceQueryFindByPattern persistenceQuery) {

        

        
//        final Object pojoPattern = persistenceQuery.getPattern().getObject();
//        
//        final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
//        
//        
//        final CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
//        
//        final Query query = getEntityManager().createQuery(criteriaQuery);
//        final List<?> results = query.getResultList();
//        return loadAdapters(persistenceQuery.getSpecification(), results);
        
        throw new NotYetImplementedException();
    }
}

// Copyright (c) Naked Objects Group Ltd.
