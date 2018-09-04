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
package org.apache.isis.core.runtime.system.persistence.adaptermanager;

import static org.apache.isis.commons.internal.functions._Predicates.equalTo;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;

/**
 *  
 * @since 2.0.0-M2
 */
class ObjectAdapterContext_Consistency {
    
    private static final Logger LOG = LoggerFactory.getLogger(ObjectAdapterContext_Consistency.class);
    private final ObjectAdapterContext objectAdapterContext;
    
    ObjectAdapterContext_Consistency(ObjectAdapterContext objectAdapterContext) {
        this.objectAdapterContext = objectAdapterContext;
    }

    /**
     * Fail early if any problems.
     * @deprecated https://issues.apache.org/jira/browse/ISIS-1976
     */
    protected void ensureMapsConsistent(final ObjectAdapter adapter) {
        if (adapter.isValue()) {
            return;
        }
        if (adapter.isParentedCollection()) {
            return;
        }
        ensurePojoAdapterMapConsistent(adapter);
        ensureOidAdapterMapConsistent(adapter);
    }

    /**
     * Fail early if any problems.
     * @deprecated https://issues.apache.org/jira/browse/ISIS-1976
     */
    protected void ensureMapsConsistent(final Oid oid) {
        Objects.requireNonNull(oid);

        final ObjectAdapter adapter = objectAdapterContext.lookupAdapterById(oid);
        if (adapter == null) {
            return;
        }
        ensureOidAdapterMapConsistent(adapter);
        ensurePojoAdapterMapConsistent(adapter);
    }

    private void ensurePojoAdapterMapConsistent(final ObjectAdapter adapter) {
        final Object adapterPojo = adapter.getObject();
        final ObjectAdapter adapterAccordingToMap = objectAdapterContext.lookupAdapterByPojo(adapterPojo);

        if(adapterPojo == null) {
            // nothing to check
            return;
        }
        ensureMapConsistent(adapter, adapterAccordingToMap, "PojoAdapterMap");
    }

    private void ensureOidAdapterMapConsistent(final ObjectAdapter adapter) {
        final Oid adapterOid = adapter.getOid();
        final ObjectAdapter adapterAccordingToMap = objectAdapterContext.lookupAdapterById(adapterOid);

        if(adapterOid == null) {
            // nothing to check
            return;
        }
        ensureMapConsistent(adapter, adapterAccordingToMap, "OidAdapterMap");
    }

    private void ensureMapConsistent(
            final ObjectAdapter adapter,
            final ObjectAdapter adapterAccordingToMap,
            final String mapName) {

        final Oid adapterOid = adapter.getOid();

        // take care not to touch the pojo, since it might have been deleted.

        if(adapterAccordingToMap == null) {
            throw new IllegalStateException("mismatch in "
                    + mapName
                    + ": provided adapter's OID: " + adapterOid + "; but no adapter found in map");
        }

        ensureThatArg(
                adapter, equalTo(adapterAccordingToMap),
                ()->"mismatch in "
                        + mapName
                        + ": provided adapter's OID: " + adapterOid + ", \n"
                        + "but map's adapter's OID was: " + adapterAccordingToMap.getOid());
    }
}