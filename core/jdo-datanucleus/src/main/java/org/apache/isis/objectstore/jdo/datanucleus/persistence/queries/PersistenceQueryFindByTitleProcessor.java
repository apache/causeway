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

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindByTitle;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.FrameworkSynchronizer;

public class PersistenceQueryFindByTitleProcessor extends PersistenceQueryProcessorAbstract<PersistenceQueryFindByTitle> {

    public PersistenceQueryFindByTitleProcessor(final PersistenceManager persistenceManager, final FrameworkSynchronizer frameworkSynchronizer) {
        super(persistenceManager, frameworkSynchronizer);
    }

    public List<ObjectAdapter> process(final PersistenceQueryFindByTitle persistenceQuery) {
        final ObjectSpecification objectSpec = persistenceQuery.getSpecification();
        final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
        return process(persistenceQuery, correspondingClass);
    }

    private <Z> List<ObjectAdapter> process(final PersistenceQueryFindByTitle persistenceQuery, Class<Z> correspondingClass) {
        throw new NotYetImplementedException();
    }
}
