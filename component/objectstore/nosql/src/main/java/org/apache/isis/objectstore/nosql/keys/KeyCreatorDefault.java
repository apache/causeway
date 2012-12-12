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

package org.apache.isis.objectstore.nosql.keys;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.nosql.NoSqlStoreException;

public class KeyCreatorDefault {

    /**
     * returns {@link RootOid#getIdentifier()} (oid must be {@link RootOid}, and must be persistent). 
     */
    public String getIdentifierForPersistentRoot(final Oid oid) {
        if (!(oid instanceof RootOid)) {
            throw new NoSqlStoreException("Oid is not a RootOid: " + oid);
        } 
        RootOid rootOid = (RootOid) oid;
        if (rootOid.isTransient()) {
            throw new NoSqlStoreException("Oid is not for a persistent object: " + oid);
        }
        return rootOid.getIdentifier();
    }

    /**
     * Equivalent to the {@link Oid#enString(OidMarshaller)} for the adapter's Oid.
     */
    public String oidStrFor(final ObjectAdapter adapter) {
        if(adapter == null) {
            return null;
        }
        try {
            //return adapter.getSpecification().getFullIdentifier() + "@" + key(adapter.getOid());
            return adapter.getOid().enString(getOidMarshaller());
        } catch (final NoSqlStoreException e) {
            throw new NoSqlStoreException("Failed to create refence for " + adapter, e);
        }
    }

    public RootOid createRootOid(ObjectSpecification objectSpecification, final String identifier) {
        final ObjectSpecId objectSpecId = objectSpecification.getSpecId();
        return RootOidDefault.create(objectSpecId, identifier);
    }

    public RootOid unmarshal(final String oidStr) {
//        final ObjectSpecification objectSpecification = specificationFromReference(ref);
//        final String id = ref.split("@")[1];
//        return oid(objectSpecification, id);
        return getOidMarshaller().unmarshal(oidStr, RootOid.class);
    }

    public ObjectSpecification specificationFromOidStr(final String oidStr) {
//        final String name = ref.split("@")[0];
//        return getSpecificationLoader().loadSpecification(name);
        final TypedOid oid = getOidMarshaller().unmarshal(oidStr, TypedOid.class);
        return getSpecificationLoader().lookupBySpecId(oid.getObjectSpecId());
    }

    
    /////////////////////////////////////////////////
    // dependencies (from context)
    /////////////////////////////////////////////////
    
    
    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }


}
