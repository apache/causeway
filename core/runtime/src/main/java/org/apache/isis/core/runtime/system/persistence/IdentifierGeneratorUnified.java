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
package org.apache.isis.core.runtime.system.persistence;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.runtime.PersistorImplementation;

public class IdentifierGeneratorUnified implements IdentifierGenerator {

    private final IdentifierGenerator identifierGenerator;

    public IdentifierGeneratorUnified(final IsisConfiguration configuration) {
        this.identifierGenerator = PersistorImplementation.from(configuration).isDataNucleus() ? new IdentifierGeneratorForDataNucleus(): new IdentifierGeneratorDefault();
    }

    public String createTransientIdentifierFor(ObjectSpecId objectSpecId, final Object pojo) {
        return identifierGenerator.createTransientIdentifierFor(objectSpecId, pojo);
    }

    public String createAggregateLocalId(ObjectSpecId objectSpecId, final Object pojo, final ObjectAdapter parentAdapter) {
        return identifierGenerator.createAggregateLocalId(objectSpecId, pojo, parentAdapter);
    }
    
    public String createPersistentIdentifierFor(ObjectSpecId objectSpecId, Object pojo, RootOid transientRootOid) {
        return identifierGenerator.createPersistentIdentifierFor(objectSpecId, pojo, transientRootOid);
    }

    public <T extends IdentifierGenerator> T underlying(Class<T> cls) {
        return cls.isAssignableFrom(identifierGenerator.getClass()) ? (T) cls.cast(identifierGenerator) : null;
    }

    @Override
    public String debugTitle() {
        return identifierGenerator.debugTitle();
    }

    @Override
    public void debugData(DebugBuilder debug) {
        identifierGenerator.debugData(debug);
    }



}
