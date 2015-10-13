/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.InvokedOn;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.services.actinvoc.ActionInvocationContext;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command.Executor;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.facets.actions.bulk.BulkFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetInferred;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberDependencies;

public class ObjectActionMixedIn extends ObjectActionDefault implements MixedInMember {

    /**
     * The type of the mixin (providing the action), eg annotated with {@link org.apache.isis.applib.annotation.Mixin}.
     */
    private final Class<?> mixinType;

    /**
     * The {@link ObjectActionDefault} for the action being mixed in (ie on the {@link #mixinType}.
     */
    private final ObjectActionDefault mixinAction;

    /**
     * The domain object type being mixed in to (being supplemented).
     */
    private final ObjectSpecification mixedInType;

    /**
     * Hold facets rather than delegate to the mixin action
     */
    private final FacetHolder facetHolder = new FacetHolderImpl();

    /**
     * Lazily initialized by {@link #getParameters()} (so don't use directly!)
     */
    private List<ObjectActionParameterMixedIn> parameters;

    private final Identifier identifier;

    public ObjectActionMixedIn(
            final Class<?> mixinType,
            final ObjectActionDefault mixinAction,
            final ObjectSpecification mixedInType,
            final ObjectMemberDependencies objectMemberDependencies) {
        super(mixinAction.getFacetedMethod(), objectMemberDependencies);

        this.mixinType = mixinType;
        this.mixinAction = mixinAction;
        this.mixedInType = mixedInType;

        // copy over facets from mixin action to self
        FacetUtil.copyFacets(mixinAction.getFacetedMethod(), facetHolder);

        // adjust name if necessary
        final String name = getName();

        String memberName = null;
        if(Objects.equal(name, "_")) {
            memberName = determineNameFrom(mixinAction);
            FacetUtil.addFacet(new NamedFacetInferred(memberName, facetHolder));
        }

        // calculate the identifier
        final Identifier mixinIdentifier = mixinAction.getFacetedMethod().getIdentifier();
        memberName = memberName != null? memberName : mixinIdentifier.getMemberName();
        List<String> memberParameterNames = mixinIdentifier.getMemberParameterNames();
        identifier = Identifier.actionIdentifier(getOnType().getCorrespondingClass().getName(), memberName, memberParameterNames);
    }

    private static String determineNameFrom(final ObjectActionDefault mixinAction) {
        return suffixAfterUnderscore(mixinAction.getOnType().getSingularName());
    }

    static String suffixAfterUnderscore(final String singularName) {
        return StringExtensions.asCapitalizedName(suffix(singularName));
    }

    private static String suffix(final String singularName) {
        if (singularName.endsWith("_")) {
            if (Objects.equal(singularName, "_")) {
                return singularName;
            }
            return singularName;
        }
        final int indexOfUnderscore = singularName.lastIndexOf('_');
        if (indexOfUnderscore == -1) {
            return singularName;
        }
        return singularName.substring(indexOfUnderscore + 1);
    }

    @Override
    public ObjectSpecification getOnType() {
        return mixedInType;
    }

    public int getParameterCount() {
        return mixinAction.getParameterCount();
    }

    public synchronized List<ObjectActionParameter> getParameters() {

        if (this.parameters == null) {
            final List<ObjectActionParameter> mixinActionParameters = mixinAction.getParameters();
            final List<ObjectActionParameterMixedIn> mixedInParameters = Lists.newArrayList();

            for (int paramNum = 0; paramNum < mixinActionParameters.size(); paramNum++ ) {

                final ObjectActionParameterAbstract mixinParameter =
                        (ObjectActionParameterAbstract) mixinActionParameters.get(paramNum);
                final ObjectActionParameterMixedIn mixedInParameter;
                mixedInParameter = new OneToOneActionParameterMixedIn(mixinParameter, this);
                mixedInParameters.add(mixedInParameter);
            }
            this.parameters = mixedInParameters;
        }
        return ObjectExtensions.asListT(parameters, ObjectActionParameter.class);
    }

    @Override
    public Consent isVisible(
            final ObjectAdapter mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        final VisibilityContext<?> ic = mixinAction.createVisibleInteractionContext(mixinAdapterFor(mixedInAdapter),
                interactionInitiatedBy, where);
        return InteractionUtils.isVisibleResult(this, ic).createConsent();
    }

    @Override
    public Consent isUsable(
            final ObjectAdapter mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy, final Where where) {
        final UsabilityContext<?> ic = mixinAction.createUsableInteractionContext(mixinAdapterFor(mixedInAdapter),
                interactionInitiatedBy, where);
        return InteractionUtils.isUsableResult(this, ic).createConsent();
    }

    @Override
    public ObjectAdapter[] getDefaults(final ObjectAdapter mixedInAdapter) {
        return mixinAction.getDefaults(mixinAdapterFor(mixedInAdapter));
    }

    @Override
    public ObjectAdapter[][] getChoices(
            final ObjectAdapter mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return mixinAction.getChoices(mixinAdapterFor(mixedInAdapter), interactionInitiatedBy);
    }

    public Consent isProposedArgumentSetValid(
            final ObjectAdapter mixedInAdapter,
            final ObjectAdapter[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return mixinAction.isProposedArgumentSetValid(mixinAdapterFor(mixedInAdapter), proposedArguments, interactionInitiatedBy);
    }

    @Override
    public ObjectAdapter execute(
            final ObjectAdapter mixedInAdapter,
            final ObjectAdapter[] arguments,
            final InteractionInitiatedBy interactionInitiatedBy) {

        // this code also exists in ActionInvocationFacetViaMethod
        // we need to repeat it here because the target adapter should be the mixedInAdapter, not the mixin

        final BulkFacet bulkFacet = getFacet(BulkFacet.class);
        if (bulkFacet != null) {

            final ActionInvocationContext actionInvocationContext =
                    getServicesInjector().lookupService(ActionInvocationContext.class);
            if (actionInvocationContext != null &&
                    actionInvocationContext.getInvokedOn() == null) {

                actionInvocationContext.setInvokedOn(InvokedOn.OBJECT);
                actionInvocationContext.setDomainObjects(Collections.singletonList(mixedInAdapter.getObject()));
            }

            final Bulk.InteractionContext bulkInteractionContext = getServicesInjector().lookupService(Bulk.InteractionContext.class);
            if (bulkInteractionContext != null &&
                    bulkInteractionContext.getInvokedAs() == null) {

                bulkInteractionContext.setInvokedAs(Bulk.InteractionContext.InvokedAs.REGULAR);
                actionInvocationContext.setDomainObjects(Collections.singletonList(mixedInAdapter.getObject()));
            }
        }

        final CommandContext commandContext = getServicesInjector().lookupService(CommandContext.class);
        final Command command = commandContext != null ? commandContext.getCommand() : null;

        if(command != null && command.getExecutor() == Executor.USER) {

            if (command.getTarget() != null) {
                // already set up by a edit form
                // don't overwrite
            } else {
                command.setTargetClass(CommandUtil.targetClassNameFor(mixedInAdapter));
                command.setTargetAction(CommandUtil.targetActionNameFor(this));
                command.setArguments(CommandUtil.argDescriptionFor(this, arguments));

                final Bookmark targetBookmark = CommandUtil.bookmarkFor(mixedInAdapter);
                command.setTarget(targetBookmark);
            }
        }

        return mixinAction.execute(mixinAdapterFor(mixedInAdapter), arguments, interactionInitiatedBy);
    }

    // //////////////////////////////////////
    // FacetHolder
    // //////////////////////////////////////

    @Override
    public Class<? extends Facet>[] getFacetTypes() {
        return facetHolder.getFacetTypes();
    }

    @Override
    public <T extends Facet> T getFacet(Class<T> cls) {
        return facetHolder.getFacet(cls);
    }

    @Override
    public boolean containsFacet(Class<? extends Facet> facetType) {
        return facetHolder.containsFacet(facetType);
    }

    @Override
    public boolean containsDoOpFacet(Class<? extends Facet> facetType) {
        return facetHolder.containsDoOpFacet(facetType);
    }

    @Override
    public List<Facet> getFacets(Filter<Facet> filter) {
        return facetHolder.getFacets(filter);
    }

    @Override
    public void addFacet(Facet facet) {
        facetHolder.addFacet(facet);
    }

    @Override
    public void addFacet(MultiTypedFacet facet) {
        facetHolder.addFacet(facet);
    }
    
    @Override
    public void removeFacet(Facet facet) {
        facetHolder.removeFacet(facet);
    }

    @Override
    public void removeFacet(Class<? extends Facet> facetType) {
        facetHolder.removeFacet(facetType);
    }

    
    // //////////////////////////////////////
    
    /* (non-Javadoc)
     * @see org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract#getIdentifier()
     */
    @Override
    public Identifier getIdentifier() {
        return identifier;
    }
    
    // //////////////////////////////////////

    ObjectAdapter mixinAdapterFor(final ObjectAdapter mixedInAdapter) {
        final ObjectSpecification objectSpecification = getSpecificationLoader().loadSpecification(mixinType);
        final MixinFacet mixinFacet = objectSpecification.getFacet(MixinFacet.class);
        final Object mixinPojo = mixinFacet.instantiate(mixedInAdapter.getObject());
        return getPersistenceSessionService().adapterFor(mixinPojo);
    }
}
