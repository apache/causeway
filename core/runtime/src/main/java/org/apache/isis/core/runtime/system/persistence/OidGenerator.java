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

import java.util.UUID;

import javax.jdo.PersistenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.Oid.State;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.spi.JdoObjectIdSerializer;

public class OidGenerator implements DebuggableWithTitle {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(OidGenerator.class);

    //region > constructor 
    private final PersistenceSession persistenceSession;
    private final SpecificationLoaderSpi specificationLoader;

    public OidGenerator(
            final PersistenceSession persistenceSession,
            final SpecificationLoaderSpi specificationLoader) {
        this.persistenceSession = persistenceSession;
        this.specificationLoader = specificationLoader;
    }
    //endregion

    //region > create...Oid (main API)
    /**
     * Create a new {@link Oid#isTransient() transient} {@link Oid} for the
     * supplied pojo, uniquely distinguishable from any other {@link Oid}.
     */
    public final RootOid createTransientOrViewModelOid(final Object pojo) {
        return newIdentifier(pojo, Type.TRANSIENT);
    }

    /**
     * Return an equivalent {@link RootOid}, but being persistent.
     * 
     * <p>
     * It is the responsibility of the implementation to determine the new unique identifier.
     * For example, the generator may simply assign a new value from a sequence, or a GUID;
     * or, the generator may use the oid to look up the object and inspect the object in order
     * to obtain an application-defined value.  
     * 
     * @param pojo - being persisted
     */
    public final RootOid createPersistentOrViewModelOid(Object pojo) {
        return newIdentifier(pojo, Type.PERSISTENT);
    }

    //endregion

    //region > helpers

    enum Type {
        TRANSIENT, PERSISTENT
    }

    private RootOid newIdentifier(final Object pojo, final OidGenerator.Type type) {
        final ObjectSpecification spec = objectSpecFor(pojo);
        if(spec.isService()) {
            return newRootId(spec, "1", type);
        }

        final ViewModelFacet recreatableObjectFacet = spec.getFacet(ViewModelFacet.class);
        final String identifier =
                recreatableObjectFacet != null
                        ? recreatableObjectFacet.memento(pojo)
                        : newIdentifierFor(pojo, type);

        return newRootId(spec, identifier, type);
    }

    private String newIdentifierFor(final Object pojo, final Type type) {
        return type == Type.TRANSIENT
                ? UUID.randomUUID().toString()
                : JdoObjectIdSerializer.toOidIdentifier(getJdoPersistenceManager().getObjectId(pojo));
    }

    private RootOid newRootId(final ObjectSpecification spec, final String identifier, final Type type) {
        final State state =
                spec.containsDoOpFacet(ViewModelFacet.class)
                    ? State.VIEWMODEL
                    : type == Type.TRANSIENT
                        ? State.TRANSIENT
                        : State.PERSISTENT;
        final ObjectSpecId objectSpecId = spec.getSpecId();
        return new RootOid(objectSpecId, identifier, state);
    }

    private ObjectSpecification objectSpecFor(final Object pojo) {
        final Class<?> pojoClass = pojo.getClass();
        return getSpecificationLoader().loadSpecification(pojoClass);
    }
    //endregion

    //region > dependencies (from constructor)
    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    protected PersistenceManager getJdoPersistenceManager() {
        return persistenceSession.getPersistenceManager();
    }
    //endregion

    //region > debug
    @Override
    public void debugData(final DebugBuilder debug) {
    }


    @Override
    public String debugTitle() {
        return "OidGenerator";
    }

    //endregion



}
