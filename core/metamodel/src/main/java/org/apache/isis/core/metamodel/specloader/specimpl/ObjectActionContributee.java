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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.actinvoc.ActionInvocationContext;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.InvokedOn;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command.Executor;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.core.metamodel.facets.actions.bulk.BulkFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;

public class ObjectActionContributee extends ObjectActionImpl implements ContributeeMember {

    private final ObjectAdapter serviceAdapter;
    private final ObjectActionImpl serviceAction;
    private final int contributeeParam;
    private final ObjectSpecification contributeeType;
    
    /**
     * Hold facets rather than delegate to the contributed action (different types might
     * use layout metadata to position the contributee in different ways)
     */
    private final FacetHolder facetHolder = new FacetHolderImpl();

    /**
     * Lazily initialized by {@link #getParameters()} (so don't use directly!)
     */
    private List<ObjectActionParameterContributee> parameters;
    
    
    private final Identifier identifier;

    /**
     * @param contributeeParam - the parameter number which corresponds to the contributee, and so should be suppressed.
     */
    public ObjectActionContributee(
            final ObjectAdapter serviceAdapter,
            final ObjectActionImpl serviceAction,
            final int contributeeParam,
            final ObjectSpecification contributeeType,
            final ObjectMemberContext objectMemberContext) {
        super(serviceAction.getFacetedMethod(), objectMemberContext);
        
        this.serviceAdapter = serviceAdapter;
        this.serviceAction = serviceAction;
        this.contributeeType = contributeeType;
        this.contributeeParam = contributeeParam;

        // copy over facets from contributed to own.
        FacetUtil.copyFacets(serviceAction.getFacetedMethod(), facetHolder);

        // calculate the identifier
        final Identifier contributorIdentifier = serviceAction.getFacetedMethod().getIdentifier();
        final String memberName = contributorIdentifier.getMemberName();
        List<String> memberParameterNames = contributorIdentifier.getMemberParameterNames();
        identifier = Identifier.actionIdentifier(getOnType().getCorrespondingClass().getName(), memberName, memberParameterNames);
    }

    @Override
    public ObjectSpecification getOnType() {
        return contributeeType;
    }

    public int getParameterCount() {
        return serviceAction.getParameterCount() - 1;
    }

    public int getContributeeParam() {
        return contributeeParam;
    }

    @Override
    public boolean isContributedBy(final ObjectAction serviceAction) {
        return serviceAction == this.serviceAction;
    }

    @Override
    public int getContributeeParamPosition() {
        return contributeeParam;
    }

    public synchronized List<ObjectActionParameter> getParameters() {
        
        if (this.parameters == null) {
            final List<ObjectActionParameter> serviceParameters = serviceAction.getParameters();
            
            final List<ObjectActionParameterContributee> contributeeParameters = Lists.newArrayList();
            
            int contributeeParamNum = 0;
            for (int serviceParamNum = 0; serviceParamNum < serviceParameters.size(); serviceParamNum++ ) {
                if(serviceParamNum == contributeeParam) {
                    // skip so is omitted from the Contributed action
                    continue;
                }
                
                final ObjectActionParameterAbstract serviceParameter = 
                        (ObjectActionParameterAbstract) serviceParameters.get(serviceParamNum);
                final ObjectActionParameterContributee contributedParam;
                if(serviceParameter instanceof ObjectActionParameterParseable) {
                    contributedParam = new ObjectActionParameterParseableContributee(
                            serviceAdapter, serviceAction, serviceParameter, serviceParamNum, 
                            contributeeParamNum, this);
                } else if(serviceParameter instanceof OneToOneActionParameterImpl) {
                    contributedParam = new OneToOneActionParameterContributee(
                            serviceAdapter, serviceAction, serviceParameter, serviceParamNum, 
                            contributeeParamNum, this);
                } else {
                    throw new RuntimeException("Unknown implementation of ObjectActionParameter; " + serviceParameter.getClass().getName());
                }
                contributeeParameters.add(contributedParam);
                
                contributeeParamNum++;
            }
            this.parameters = contributeeParameters;
        }
        return ObjectExtensions.asListT(parameters, ObjectActionParameter.class);
    }

    
    @Override
    public Consent isVisible(final AuthenticationSession session, final ObjectAdapter contributee, Where where) {
        final VisibilityContext<?> ic = serviceAction.createVisibleInteractionContext(session, InteractionInvocationMethod.BY_USER, serviceAdapter, where);
        ic.putContributee(this.contributeeParam, contributee);
        return InteractionUtils.isVisibleResult(this, ic).createConsent();
    }

    @Override
    public Consent isUsable(final AuthenticationSession session, final ObjectAdapter contributee, Where where) {
        final UsabilityContext<?> ic = serviceAction.createUsableInteractionContext(session, InteractionInvocationMethod.BY_USER, serviceAdapter, where);
        ic.putContributee(this.contributeeParam, contributee);
        return InteractionUtils.isUsableResult(this, ic).createConsent();
    }

    @Override
    public ObjectAdapter[] getDefaults(final ObjectAdapter target) {
        final ObjectAdapter[] contributorDefaults = serviceAction.getDefaults(serviceAdapter);
        return removeElementFromArray(contributorDefaults, contributeeParam, new ObjectAdapter[]{});
    }

    @Override
    public ObjectAdapter[][] getChoices(final ObjectAdapter target) {
        final ObjectAdapter[][] serviceChoices = serviceAction.getChoices(serviceAdapter);
        return removeElementFromArray(serviceChoices, contributeeParam, new ObjectAdapter[][]{});
    }
        
    public Consent isProposedArgumentSetValid(final ObjectAdapter contributee, final ObjectAdapter[] proposedArguments) {
        ObjectAdapter[] serviceArguments = argsPlusContributee(contributee, proposedArguments);
        return serviceAction.isProposedArgumentSetValid(serviceAdapter, serviceArguments);
    }

    @Override
    public ObjectAdapter execute(final ObjectAdapter contributee, final ObjectAdapter[] arguments) {
        
        // this code also exists in ActionInvocationFacetViaMethod
        // we need to repeat it here because the target adapter should be the contributee, not the contributing service.

        final BulkFacet bulkFacet = getFacet(BulkFacet.class);
        if (bulkFacet != null) {

            final ActionInvocationContext actionInvocationContext = getServicesProvider().lookupService(ActionInvocationContext.class);
            if (actionInvocationContext != null &&
                    actionInvocationContext.getInvokedOn() == null) {

                actionInvocationContext.setInvokedOn(InvokedOn.OBJECT);
                actionInvocationContext.setDomainObjects(Collections.singletonList(contributee.getObject()));
            }

            final Bulk.InteractionContext bulkInteractionContext = getServicesProvider().lookupService(Bulk.InteractionContext.class);
            if (bulkInteractionContext != null &&
                    bulkInteractionContext.getInvokedAs() == null) {

                bulkInteractionContext.setInvokedAs(Bulk.InteractionContext.InvokedAs.REGULAR);
                actionInvocationContext.setDomainObjects(Collections.singletonList(contributee.getObject()));
            }


        }

        final CommandContext commandContext = getServicesProvider().lookupService(CommandContext.class);
        final Command command = commandContext != null ? commandContext.getCommand() : null;

        if(command != null && command.getExecutor() == Executor.USER) {

            if (command.getTarget() != null) {
                // already set up by a edit form
                // don't overwrite
            } else {
                command.setTargetClass(CommandUtil.targetClassNameFor(contributee));
                command.setTargetAction(CommandUtil.targetActionNameFor(this));
                command.setArguments(CommandUtil.argDescriptionFor(this, arguments));

                final Bookmark targetBookmark = CommandUtil.bookmarkFor(contributee);
                command.setTarget(targetBookmark);
            }
        }
        
        return serviceAction.execute(serviceAdapter, argsPlusContributee(contributee, arguments));
    }

    private ObjectAdapter[] argsPlusContributee(final ObjectAdapter contributee, final ObjectAdapter[] arguments) {
        return addElementToArray(arguments, contributeeParam, contributee, new ObjectAdapter[]{});
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
    public boolean containsDoOpFacet(java.lang.Class<? extends Facet> facetType) {
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

    static <T> T[] addElementToArray(T[] array, final int n, final T element, final T[] type) {
        List<T> list = Lists.newArrayList(Arrays.asList(array));
        list.add(n, element);
        return list.toArray(type);
    }

    static <T> T[] removeElementFromArray(T[] array, int n, T[] t) {
        List<T> list = Lists.newArrayList(Arrays.asList(array));
        list.remove(n);
        return list.toArray(t);
    }

}
