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


package org.apache.isis.metamodel.runtimecontext.spec.feature;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.metamodel.consent.InteractionResult;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.MultiTypedFacet;
import org.apache.isis.metamodel.facets.help.HelpFacet;
import org.apache.isis.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.metamodel.facets.naming.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.isis.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.isis.metamodel.interactions.InteractionUtils;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.spec.identifier.Identified;
import org.apache.isis.metamodel.util.NameUtils;


public abstract class ObjectMemberAbstract implements ObjectMember {

    /**
     * powertype for subclasses.
     */
    protected static enum MemberType {
        ONE_TO_ONE_ASSOCIATION,
        ONE_TO_MANY_ASSOCIATION,
        ACTION;
        public boolean isAction() {
            return this == ACTION;
        }
        public boolean isAssociation() {
            return !isAction();
        }
        public boolean isOneToManyAssociation() {
            return this == ONE_TO_MANY_ASSOCIATION;
        }
        public boolean isOneToOneAssociation() {
            return this == ONE_TO_ONE_ASSOCIATION;
        }
    }
    
    protected final String defaultName;
    private final String id;
    private final Identified facetHolder;
    private final MemberType memberType;
	private RuntimeContext runtimeContext;

    protected ObjectMemberAbstract(
    		final String id, 
    		final Identified facetHolder, 
    		final MemberType memberType, 
    		final RuntimeContext runtimeContext) {
        if (id == null) {
            throw new IllegalArgumentException("Name must always be set");
        }
        this.id = id;
        this.defaultName = NameUtils.naturalName(id);
        this.facetHolder = facetHolder;
        this.memberType = memberType;
        this.runtimeContext = runtimeContext;
    }

    // /////////////////////////////////////////////////////////////
    // Identifiers
    // /////////////////////////////////////////////////////////////

    public String getId() {
        return id;
    }

    public Identifier getIdentifier() {
        return facetHolder.getIdentifier();
    }

    @Override
    public String toString() {
        return String.format("id=%s,name='%s'", getId(), getName());
    }

    // /////////////////////////////////////////////////////////////
    // Facets
    // /////////////////////////////////////////////////////////////

    public boolean containsFacet(final Class<? extends Facet> facetType) {
        return facetHolder.containsFacet(facetType);
    }

    public <T extends Facet> T getFacet(final Class<T> cls) {
        return facetHolder.getFacet(cls);
    }

    public Class<? extends Facet>[] getFacetTypes() {
        return facetHolder.getFacetTypes();
    }

    public Facet[] getFacets(final Filter<Facet> filter) {
        return facetHolder.getFacets(filter);
    }

    public void addFacet(final Facet facet) {
        facetHolder.addFacet(facet);
    }

    public void addFacet(final MultiTypedFacet facet) {
        facetHolder.addFacet(facet);
    }

    public void removeFacet(final Facet facet) {
        facetHolder.removeFacet(facet);
    }

    public void removeFacet(final Class<? extends Facet> facetType) {
        facetHolder.removeFacet(facetType);
    }

    // /////////////////////////////////////////////////////////////
    // Name, Description, Help (convenience for facets)
    // /////////////////////////////////////////////////////////////

    /**
     * Return the default label for this member. This is based on the name of this member.
     * 
     * @see #getId()
     */
    public String getName() {
        final NamedFacet facet = getFacet(NamedFacet.class);
        String name = facet.value();
        name = name == null ? defaultName : name;
        return name;
    }

    public String getDescription() {
        final DescribedAsFacet facet = getFacet(DescribedAsFacet.class);
        return facet.value();
    }

    public String getHelp() {
        final HelpFacet facet = getFacet(HelpFacet.class);
        return facet.value();
    }

    // /////////////////////////////////////////////////////////////
    // Hidden (or visible)
    // /////////////////////////////////////////////////////////////


    public boolean isAlwaysHidden() {
        return containsFacet(HiddenFacet.class);
    }
    
    /**
     * Loops over all {@link HidingInteractionAdvisor} {@link Facet}s and returns <tt>true</tt> only if none
     * hide the member.
     * 
     * <p>
     * TODO: currently this method is hard-coded to assume all interactions are initiated
     * {@link InteractionInvocationMethod#BY_USER by user}.
     */
    public Consent isVisible(final AuthenticationSession session, final ObjectAdapter target) {
        return isVisibleResult(session, target).createConsent();
    }

    private InteractionResult isVisibleResult(final AuthenticationSession session, final ObjectAdapter target) {
        final VisibilityContext<?> ic = createVisibleInteractionContext(session, InteractionInvocationMethod.BY_USER, target);
        return InteractionUtils.isVisibleResult(this, ic);
    }

    // /////////////////////////////////////////////////////////////
    // Disabled (or enabled)
    // /////////////////////////////////////////////////////////////

    /**
     * Loops over all {@link DisablingInteractionAdvisor} {@link Facet}s and returns <tt>true</tt> only if
     * none disables the member.
     * 
     * <p>
     * TODO: currently this method is hard-coded to assume all interactions are initiated
     * {@link InteractionInvocationMethod#BY_USER by user}.
     */
    public Consent isUsable(final AuthenticationSession session, final ObjectAdapter target) {
        return isUsableResult(session, target).createConsent();
    }

    private InteractionResult isUsableResult(final AuthenticationSession session, final ObjectAdapter target) {
        final UsabilityContext<?> ic = createUsableInteractionContext(session, InteractionInvocationMethod.BY_USER, target);
        return InteractionUtils.isUsableResult(this, ic);
    }


    // //////////////////////////////////////////////////////////////////
    // isAssociation, isAction
    // //////////////////////////////////////////////////////////////////

    public boolean isAction() {
        return memberType.isAction();
    }

    public boolean isAssociation() {
        return memberType.isAssociation();
    }

    public boolean isOneToManyAssociation() {
        return memberType.isOneToManyAssociation();
    }

    public boolean isOneToOneAssociation() {
        return memberType.isOneToOneAssociation();
    }


    // //////////////////////////////////////////////////////////////////
    // RuntimeContext
    // //////////////////////////////////////////////////////////////////

    public RuntimeContext getRuntimeContext() {
    	return runtimeContext;
    }
    
    protected AuthenticationSession getAuthenticationSession() {
        return getRuntimeContext().getAuthenticationSession();
    }

}
