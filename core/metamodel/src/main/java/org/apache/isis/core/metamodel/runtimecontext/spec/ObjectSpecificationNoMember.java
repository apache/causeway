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

package org.apache.isis.core.metamodel.runtimecontext.spec;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecoratorSet;
import org.apache.isis.core.metamodel.interactions.InteractionContext;
import org.apache.isis.core.metamodel.runtimecontext.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.runtimecontext.ObjectInstantiator;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.util.NameUtils;

/**
 * A simple implementation used for objects that have no members (fields or actions). Used for primitives and as a
 * fall-back if no specification can be otherwise generated.
 */
public class ObjectSpecificationNoMember extends IntrospectableSpecificationAbstract {
    private final String name;

    public ObjectSpecificationNoMember(final String className,
        final AuthenticationSessionProvider authenticationSessionProvider, final ObjectInstantiator objectInstantiator) {
        super(authenticationSessionProvider, null, objectInstantiator);
        this.fullName = className;
        this.name = NameUtils.simpleName(className.substring(className.lastIndexOf('.') + 1));
        identifier = Identifier.classIdentifier(className);

        throw new UnexpectedCallException(className);

    }

    @Override
    public void markAsService() {
    }

    @Override
    public void introspect(final FacetDecoratorSet decorator) {
        fields = Collections.emptyList();
        superClassSpecification = null;

        setIntrospected(true);
    }

    @Override
    public String getTitle(final ObjectAdapter adapter) {
        return "no title";
    }

    @Override
    public String getShortName() {
        return name;
    }

    @Override
    public String getSingularName() {
        return name;
    }

    @Override
    public String getPluralName() {
        return name;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public ObjectAssociation getAssociation(final String name) {
        return null;
    }

    @Override
    public List<ObjectAction> getObjectActions(final ObjectActionType... type) {
        return Collections.emptyList();
    }

    @Override
    public ObjectAction getObjectAction(final ObjectActionType type, final String id,
        final List<ObjectSpecification> parameters) {
        return null;
    }

    @Override
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
    public InteractionContext createPersistInteractionContext(final AuthenticationSession session,
        final boolean programmatic, final ObjectAdapter targetObjectAdapter) {
        return null;
    }

}
