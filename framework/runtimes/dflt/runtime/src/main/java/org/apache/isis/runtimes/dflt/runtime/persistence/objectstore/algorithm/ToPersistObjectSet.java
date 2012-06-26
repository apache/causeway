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

package org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

/**
 * Set of {@link ObjectAdapter}s that require persisting.
 * 
 * <p>
 * Is consumed by {@link PersistAlgorithm}, and is ultimately implemented by
 * {@link PersistenceSession}.
 */
public interface ToPersistObjectSet {

    void remapAsPersistent(final ObjectAdapter object);
    
    void addCreateObjectCommand(ObjectAdapter object);
    
    /**
     * To support ISIS-234; keep track, for the duration of the transaction only, 
     * of the old transient {@link Oid}s and their corresponding persistent {@link Oid}s.
     */
    Oid remappedFrom(Oid oid);
    
}
