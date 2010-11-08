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


package org.apache.isis.metamodel.runtimecontext.spec;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.Veto;
import org.apache.isis.metamodel.facetdecorator.FacetDecoratorSet;
import org.apache.isis.metamodel.interactions.InteractionContext;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.Persistability;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.util.NameUtils;


/**
 * A simple implementation used for objects that have no members (fields or actions). Used for primitives and
 * as a fall-back if no specification can be otherwise generated.
 */
public class ObjectSpecificationNoMember extends IntrospectableSpecificationAbstract {
    private final String name;

    public ObjectSpecificationNoMember(
    		final String className, final RuntimeContext runtimeContext) {
    	super(runtimeContext);
        this.fullName = className;
        this.name = NameUtils.simpleName(className.substring(className.lastIndexOf('.') + 1));
        identifier = Identifier.classIdentifier(className);

        throw new UnexpectedCallException(className);

    }

    public void markAsService() {}

    public void introspect(final FacetDecoratorSet decorator) {
        fields = new ObjectAssociation[0];
        superClassSpecification = null;
        
        setIntrospected(true);
    }

    public String getTitle(final ObjectAdapter adapter) {
        return "no title";
    }

    public String getShortName() {
        return name;
    }

    public String getSingularName() {
        return name;
    }

    public String getPluralName() {
        return name;
    }

    public String getDescription() {
        return name;
    }

    public ObjectAssociation getAssociation(final String name) {
        return null;
    }

    @Override
    public ObjectAction[] getObjectActions(final ObjectActionType... type) {
        return new ObjectAction[0];
    }

    public ObjectAction getObjectAction(
            final ObjectActionType type,
            final String id,
            final ObjectSpecification[] parameters) {
        return null;
    }

    public ObjectAction getObjectAction(final ObjectActionType type, final String id) {
        return null;
    }

    @Override
    public Consent isValid(final ObjectAdapter transientObject) {
        return Veto.DEFAULT;
    }

    @Override
    public Persistability persistability() {
        return Persistability.TRANSIENT;
    }

    public void debugData(final DebugString debug) {
        debug.append("There are no reflective actions");
    }

    public String debugTitle() {
        return "NO Member Specification";
    }

    /**
     * Does nothing, but should never be called.
     */
    public InteractionContext createPersistInteractionContext(
            final AuthenticationSession session,
            final boolean programmatic,
            final ObjectAdapter targetObjectAdapter) {
        return null;
    }

}
