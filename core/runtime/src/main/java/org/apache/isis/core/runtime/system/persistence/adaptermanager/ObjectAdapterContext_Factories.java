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

import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.persistence.adapter.PojoAdapter;
import org.apache.isis.core.runtime.system.context.session.RuntimeContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.persistence.adaptermanager.ObjectAdapterContext.ObjectAdapterFactories;
import org.apache.isis.core.runtime.system.session.IsisSession;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.RequiredArgsConstructor;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: provides ObjectAdapter factories
 * </p>
 * @since 2.0.0-M2
 */
//@Log4j2
@RequiredArgsConstructor
class ObjectAdapterContext_Factories implements ObjectAdapterFactories {

    private final RuntimeContext runtimeContext;
    private final PersistenceSession persistenceSession;

    @Override
    public ObjectAdapter createRootAdapter(final Object pojo, RootOid rootOid) {
        requires(rootOid, "rootOid");
        return createAdapter(pojo, rootOid);
    }

    @Override
    public ObjectAdapter createCollectionAdapter(
            final Object pojo,
            ParentedOid collectionOid) {
        requires(collectionOid, "collectionOid");
        return createAdapter(pojo, collectionOid);
    }

    @Override
    public ObjectAdapter createCollectionAdapter(
            final Object pojo,
            final RootOid parentOid,
            final OneToManyAssociation otma) {

        Assert.assertNotNull(pojo);

        // persistence of collection follows the parent
        final ParentedOid collectionOid = Oid.Factory.parentedOfOneToMany(parentOid, otma);
        final ObjectAdapter collectionAdapter = createCollectionAdapter(pojo, collectionOid);
        return collectionAdapter;
    }

    private ObjectAdapter createAdapter(
            final Object pojo,
            final Oid oid) {
        return PojoAdapter.of(
                pojo, oid,
                IsisSession.currentOrElseNull(),
                persistenceSession);
    }
}