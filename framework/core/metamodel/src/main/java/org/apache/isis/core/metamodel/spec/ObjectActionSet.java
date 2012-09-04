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

package org.apache.isis.core.metamodel.spec;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.core.metamodel.interactions.ActionInvocationContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

public class ObjectActionSet implements ObjectAction {

    private final String name;
    private final String id;
    private final List<ObjectAction> actions;

    public ObjectActionSet(final String id, final String name, final List<ObjectAction> actions) {
        this.id = id;
        this.name = name;
        this.actions = actions;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FeatureType getFeatureType() {
        return FeatureType.ACTION;
    }

    // /////////////////////////////////////////////////////////////
    // description, actions
    // /////////////////////////////////////////////////////////////

    @Override
    public List<ObjectAction> getActions() {
        return actions;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Identifier getIdentifier() {
        return null;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public ObjectSpecification getOnType() {
        return null;
    }

    @Override
    public ObjectSpecification getReturnType() {
        return null;
    }

    @Override
    public ActionType getType() {
        return ActionType.SET;
    }

    @Override
    public boolean hasReturn() {
        return false;
    }

    @Override
    public boolean isContributed() {
        return false;
    }

    /**
     * Always returns <tt>null</tt>.
     */
    @Override
    public ObjectSpecification getSpecification() {
        return null;
    }

    @Override
    public ActionSemantics.Of getSemantics() {
        return null;
    }

    // /////////////////////////////////////////////////////////////
    // target
    // /////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter realTarget(final ObjectAdapter target) {
        return null;
    }

    // /////////////////////////////////////////////////////////////
    // execute
    // /////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter execute(final ObjectAdapter target, final ObjectAdapter[] parameters) {
        throw new UnexpectedCallException();
    }

    // /////////////////////////////////////////////////////////////
    // facets
    // /////////////////////////////////////////////////////////////

    /**
     * Does nothing
     */
    @Override
    public <T extends Facet> T getFacet(final Class<T> cls) {
        return null;
    }

    /**
     * Does nothing
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Facet>[] getFacetTypes() {
        return new Class[0];
    }

    /**
     * Does nothing
     */
    @Override
    public List<Facet> getFacets(final Filter<Facet> filter) {
        return Lists.newArrayList();
    }

    /**
     * Does nothing
     */
    @Override
    public void addFacet(final Facet facet) {
    }

    /**
     * Does nothing
     */
    @Override
    public void addFacet(final MultiTypedFacet facet) {
    }

    /**
     * Does nothing
     */
    @Override
    public void removeFacet(final Facet facet) {
    }

    /**
     * Does nothing
     */
    @Override
    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return false;
    }

    /**
     * Does nothing
     */
    @Override
    public boolean containsDoOpFacet(final Class<? extends Facet> facetType) {
        return false;
    }

    /**
     * Does nothing
     */
    @Override
    public void removeFacet(final Class<? extends Facet> facetType) {
    }

    // /////////////////////////////////////////////////////////////
    // parameters
    // /////////////////////////////////////////////////////////////

    @Override
    public int getParameterCount() {
        return 0;
    }

    @Override
    public ObjectActionParameter getParameterById(final String paramId) {
        return null;
    }

    @Override
    public ObjectActionParameter getParameterByName(final String paramName) {
        return null;
    }

    @Override
    public List<ObjectActionParameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public List<ObjectSpecification> getParameterTypes() {
        return Collections.emptyList();
    }

    @Override
    public List<ObjectActionParameter> getParameters(final Filter<ObjectActionParameter> filter) {
        return Collections.emptyList();
    }

    @Override
    public boolean promptForParameters(final ObjectAdapter target) {
        return false;
    }

    // /////////////////////////////////////////////////////////////
    // visibility
    // /////////////////////////////////////////////////////////////

    @Override
    public boolean isAlwaysHidden() {
        return false;
    }

    /**
     * Does nothing, but shouldn't be called.
     */
    @Override
    public VisibilityContext<?> createVisibleInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod invocationMethod, final ObjectAdapter targetObjectAdapter, Where where) {
        return null;
    }

    @Override
    public Consent isVisible(final AuthenticationSession session, final ObjectAdapter target, Where where) {
        return Allow.DEFAULT;
    }

    // /////////////////////////////////////////////////////////////
    // usability
    // /////////////////////////////////////////////////////////////

    @Override
    public UsabilityContext<?> createUsableInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod invocationMethod, final ObjectAdapter target, Where where) {
        return null;
    }

    @Override
    public Consent isUsable(final AuthenticationSession session, final ObjectAdapter target, Where where) {
        return Allow.DEFAULT;
    }

    // /////////////////////////////////////////////////////////////
    // validity
    // /////////////////////////////////////////////////////////////

    @Override
    public ActionInvocationContext createActionInvocationInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod invocationMethod, final ObjectAdapter object, final ObjectAdapter[] candidateArguments) {
        return null;
    }

    @Override
    public Consent isProposedArgumentSetValid(final ObjectAdapter object, final ObjectAdapter[] parameters) {
        throw new UnexpectedCallException();
    }

    // /////////////////////////////////////////////////////////////
    // defaults
    // /////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter[] getDefaults(final ObjectAdapter target) {
        throw new UnexpectedCallException();
    }

    // /////////////////////////////////////////////////////////////
    // options
    // /////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter[][] getChoices(final ObjectAdapter target) {
        throw new UnexpectedCallException();
    }

    // /////////////////////////////////////////////////////////////
    // isAction, isAssociation
    // /////////////////////////////////////////////////////////////

    @Override
    public boolean isAction() {
        return true;
    }

    @Override
    public boolean isPropertyOrCollection() {
        return false;
    }

    @Override
    public boolean isOneToManyAssociation() {
        return false;
    }

    @Override
    public boolean isOneToOneAssociation() {
        return false;
    }

    // /////////////////////////////////////////////////////////////
    // debug
    // /////////////////////////////////////////////////////////////

    @Override
    public String debugData() {
        return "";
    }

    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////

    @Override
    public Instance getInstance(final ObjectAdapter adapter) {
        final ObjectAction specification = this;
        return adapter.getInstance(specification);
    }


}
