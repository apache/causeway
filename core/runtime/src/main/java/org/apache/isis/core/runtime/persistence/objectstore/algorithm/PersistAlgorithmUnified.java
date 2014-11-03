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
package org.apache.isis.core.runtime.persistence.objectstore.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.PersistorImplementation;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;


/**
 * A {@link org.apache.isis.core.runtime.persistence.objectstore.algorithm.PersistAlgorithm} which simply saves the object made persistent.
 */
public class PersistAlgorithmUnified extends PersistAlgorithmAbstract {

    private static final Logger LOG = LoggerFactory
            .getLogger(PersistAlgorithmUnified.class);

    private final PersistAlgorithm persistAlgorithm;

    public PersistAlgorithmUnified(IsisConfiguration configuration) {
        final PersistorImplementation persistorImplementation = PersistorImplementation.from(configuration);
        persistAlgorithm = persistorImplementation.isDataNucleus()
                                ? new PersistAlgorithmForDataNucleus()
                                : new PersistAlgorithmDefault();
    }

    public String name() {
        return "PersistAlgorithmUnified";
    }

    public void makePersistent(
            final ObjectAdapter adapter,
            final PersistenceSession persistenceSession) {
        persistAlgorithm.makePersistent(adapter, persistenceSession);
    }

    @Override
    public String toString() {
        return persistAlgorithm.toString();
    }
}
