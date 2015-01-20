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

import org.apache.isis.applib.annotation.Aggregated;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.*;
import org.apache.isis.core.metamodel.adapter.oid.Oid.State;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class OidGenerator implements DebuggableWithTitle {

    private final IdentifierGenerator identifierGenerator;
    
    public OidGenerator(final IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
    }

    
    public IdentifierGenerator getIdentifierGenerator() {
        return identifierGenerator;
    }


    // //////////////////////////////////////////////////////////////
    // API and mandatory hooks
    // //////////////////////////////////////////////////////////////
    
    /**
     * Create a new {@link Oid#isTransient() transient} {@link Oid} for the
     * supplied pojo, uniquely distinguishable from any other {@link Oid}.
     *
     * TODO: the responsibility for knowing if this pojo is a view model or not are split unhappily between this class and the {@link org.apache.isis.core.runtime.system.persistence.IdentifierGenerator} impl.
     */
    public final RootOid createTransientOrViewModelOid(final Object pojo) {
        final ObjectSpecification spec = getSpecificationLookup().loadSpecification(pojo.getClass());
        final ObjectSpecId objectSpecId = spec.getSpecId();
        final String transientIdentifier = identifierGenerator.createTransientIdentifierFor(objectSpecId, pojo);
        final State state = spec.containsDoOpFacet(ViewModelFacet.class)? State.VIEWMODEL:State.TRANSIENT;
        return new RootOidDefault(objectSpecId, transientIdentifier, state);
    }

    /**
     * Creates a new id, locally unique within an aggregate root, for the specified 
     * object for use in an {@link AggregatedOid} (as returned by {@link AggregatedOid#getLocalId()}).
     * 
     * <p>
     * This is used by {@link Aggregated} references (either referenced by properties or by
     * collections).
     */
    public AggregatedOid createAggregateOid(final Object pojo, final ObjectAdapter parentAdapter) {
        final ObjectSpecId objectSpecId = objectSpecIdFor(pojo);
        final String aggregateLocalId = identifierGenerator.createAggregateLocalId(objectSpecId, pojo, parentAdapter);
        return new AggregatedOid(objectSpecId, (TypedOid) parentAdapter.getOid(), aggregateLocalId);
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
     * @param transientRootOid - the oid for the pojo when transient.
     */
    public final RootOid createPersistentOrViewModelOid(Object pojo, RootOid transientRootOid) {

        final ObjectSpecId objectSpecId = objectSpecIdFor(pojo);
        final String persistentIdentifier = identifierGenerator.createPersistentIdentifierFor(objectSpecId, pojo, transientRootOid);
        
        final ObjectSpecification spec = getSpecificationLookup().lookupBySpecId(objectSpecId);
        final State state = spec != null && spec.containsFacet(ViewModelFacet.class)? State.VIEWMODEL:State.PERSISTENT;
        return new RootOidDefault(objectSpecId, persistentIdentifier, state);
    }

    


    // //////////////////////////////////////////////////////////////
    // Helpers
    // //////////////////////////////////////////////////////////////

    private ObjectSpecId objectSpecIdFor(final Object pojo) {
        final Class<? extends Object> cls = pojo.getClass();
        final ObjectSpecification objectSpec = getSpecificationLookup().loadSpecification(cls);
        return objectSpec.getSpecId();
    }



    // //////////////////////////////////////////////////////////////
    // debug
    // //////////////////////////////////////////////////////////////


    @Override
    public void debugData(final DebugBuilder debug) {
        getIdentifierGenerator().debugData(debug);
    }


    @Override
    public String debugTitle() {
        return getIdentifierGenerator().debugTitle();
    }

    
    //////////////////////////////////////////////////////////////////
    // context
    //////////////////////////////////////////////////////////////////

    protected SpecificationLoader getSpecificationLookup() {
        return IsisContext.getSpecificationLoader();
    }

}
