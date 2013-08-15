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

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.FrameworkSynchronizer;

public class PersistenceQueryFindAllInstancesProcessor extends PersistenceQueryProcessorAbstract<PersistenceQueryFindAllInstances> {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceQueryFindAllInstancesProcessor.class);

    public PersistenceQueryFindAllInstancesProcessor(final PersistenceManager persistenceManager, final FrameworkSynchronizer frameworkSynchronizer) {
        super(persistenceManager, frameworkSynchronizer);
    }

    public List<ObjectAdapter> process(final PersistenceQueryFindAllInstances persistenceQuery) {

        final ObjectSpecification specification = persistenceQuery.getSpecification();
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstances: class=" + specification.getFullIdentifier());
        }
        
        Class<?> cls = specification.getCorrespondingClass();
        final Query query = getPersistenceManager().newQuery(cls);
        
        final List<?> pojos = (List<?>) query.execute();
        return loadAdapters(specification, pojos);
    }
}

// Copyright (c) Naked Objects Group Ltd.
