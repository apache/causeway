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

package org.apache.isis.runtimes.dflt.objectstores.nosql.keys;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.runtimes.dflt.objectstores.nosql.NoSqlStoreException;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.serial.RootOidDefault;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

public class KeyCreatorDefault implements KeyCreator {

    public KeyCreatorDefault() {
    }
    
    @Override
    public String key(final Oid oid) {
        if (!(oid instanceof RootOid)) {
            throw new NoSqlStoreException("Oid is not a RootOid: " + oid);
        } 
        RootOid rootOid = (RootOidDefault) oid;
        if (rootOid.isTransient()) {
            throw new NoSqlStoreException("Oid is not for a persistent object: " + oid);
        }
        return rootOid.getIdentifier();
    }

    @Override
    public String reference(final ObjectAdapter adapter) {
        if(adapter == null) {
            return null;
        }
        try {
            return adapter.getSpecification().getFullIdentifier() + "@" + key(adapter.getOid());
        } catch (final NoSqlStoreException e) {
            throw new NoSqlStoreException("Failed to create refence for " + adapter, e);
        }
    }

    @Override
    public RootOid oid(ObjectSpecification objectSpecification, final String id) {
        final String objectType = objectSpecification.getObjectType();
        return RootOidDefault.create(objectType, id);
    }

    @Override
    public RootOid oidFromReference(final String ref) {
        final ObjectSpecification objectSpecification = specificationFromReference(ref);
        final String id = ref.split("@")[1];
        return oid(objectSpecification, id);
    }

    @Override
    public ObjectSpecification specificationFromReference(final String ref) {
        final String name = ref.split("@")[0];
        return getSpecificationLoader().loadSpecification(name);
    }

    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

}
