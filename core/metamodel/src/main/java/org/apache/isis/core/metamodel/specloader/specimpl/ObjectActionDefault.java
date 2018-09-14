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

package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import com.google.common.base.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.InvokedOn;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.facets.actions.bulk.BulkFacet;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.interactions.ActionUsabilityContext;
import org.apache.isis.core.metamodel.interactions.ActionValidityContext;
import org.apache.isis.core.metamodel.interactions.ActionVisibilityContext;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.DomainModelException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.schema.cmd.v1.CommandDto;

public class ObjectActionDefault extends ObjectMemberAbstract implements ObjectAction {

    private final static Logger LOG = LoggerFactory.getLogger(ObjectActionDefault.class);

    public static ActionType getType(final String typeStr) {
        final ActionType type = ActionType.valueOf(typeStr);
        if (type == null) {
            throw new IllegalArgumentException();
        }
        return type;
    }

    // -- fields

    /**
     * Lazily initialized by {@link #getParameters()} (so don't use directly!)
     */
    List<ObjectActionParameter> parameters;



    // -- constructors

    public ObjectActionDefault(
            final FacetedMethod facetedMethod,
            final ServicesInjector servicesInjector) {
        super(facetedMethod, FeatureType.ACTION, servicesInjector);
    }



    // -- ReturnType, OnType, Actions (set)
    /**
     * Always returns <tt>null</tt>.
     */
    @Override
    public ObjectSpecification getSpecification() {
        return null;
    }

    @Override
    public ObjectSpecification getReturnType() {
        final ActionInvocationFacet facet = getActionInvocationFacet();
        return facet.getReturnType();
    }

    /**
     * Returns true if the represented action returns something, else returns
     * false.
     */
    @Override
    public boolean hasReturn() {
        if(getReturnType() == null) {
            // this shouldn't happen; return Type always defined, even if represents void.class
            return false;
        }
        return getReturnType() != getSpecificationLoader().loadSpecification(void.class);
    }


    @Override
    public ObjectSpecification getOnType() {
        final ActionInvocationFacet facet = getActionInvocationFacet();
        return facet.getOnType();
    }

    @Override
    public SemanticsOf getSemantics() {
        final ActionSemanticsFacet facet = getFacet(ActionSemanticsFacet.class);
        return facet != null? facet.value(): SemanticsOf.NON_IDEMPOTENT;
    }



    // -- Type
    @Override
    public ActionType getType() {
        return getType(this);
    }

    private static ActionType getType(final FacetHolder facetHolder) {
        Facet facet = facetHolder.getFacet(PrototypeFacet.class);
        if (facet != null) {
            return ActionType.PROTOTYPE;
        }
        return ActionType.USER;
    }


    // -- Parameters

    @Override
    public int getParameterCount() {
        return getFacetedMethod().getParameters().size();
    }


    /**
     * Build lazily by {@link #getParameters()}.
     *
     * <p>
     * Although this is lazily loaded, the method is also <tt>synchronized</tt>
     * so there shouldn't be any thread race conditions.
     */
    @Override
    public List<ObjectActionParameter> getParameters() {
        if (parameters == null) {
            parameters = determineParameters();
        }
        return parameters;
    }

    protected synchronized List<ObjectActionParameter> determineParameters() {
        if (parameters != null) {
            // because possible race condition (caller isn't synchronized)
            return parameters;
        }
        final int parameterCount = getParameterCount();
        final List<FacetedMethodParameter> paramPeers = getFacetedMethod().getParameters();

        final List<ObjectActionParameter> parameters = _Lists.newArrayList();
        for (int paramNum = 0; paramNum < parameterCount; paramNum++) {
            final FacetedMethodParameter paramPeer = paramPeers.get(paramNum);

            final ObjectSpecification specification = ObjectMemberAbstract
                    .getSpecification(getSpecificationLoader(), paramPeer.getType());

            // previously we threw an exception here if the specification represented a collection.  No longer!
            final ObjectActionParameter parameter =
                    paramPeer.getFeatureType() == FeatureType.ACTION_PARAMETER_SCALAR
                    ? new OneToOneActionParameterDefault(paramNum, this, paramPeer)
                            : new OneToManyActionParameterDefault(paramNum, this, paramPeer);

                    parameters.add(parameter);
        }
        return parameters;
    }

    @Override
    public List<ObjectSpecification> getParameterTypes() {
        final List<ObjectSpecification> parameterTypes = _Lists.newArrayList();
        final List<ObjectActionParameter> parameters = getParameters();
        for (final ObjectActionParameter parameter : parameters) {
            parameterTypes.add(parameter.getSpecification());
        }
        return parameterTypes;
    }

    @Override
    public ObjectActionParameter getParameterById(final String paramId) {
        final List<ObjectActionParameter> allParameters = getParameters();
        for (int i = 0; i < allParameters.size(); i++) {
            final ObjectActionParameter param = allParameters.get(i);
            if (Objects.equal(paramId, param.getId())) {
                return param;
            }
        }
        return null;
    }

    @Override
    public ObjectActionParameter getParameterByName(final String paramName) {
        final List<ObjectActionParameter> allParameters = getParameters();
        for (int i = 0; i < allParameters.size(); i++) {
            final ObjectActionParameter param = allParameters.get(i);
            if (Objects.equal(paramName, param.getName())) {
                return param;
            }
        }
        return null;
    }

    @Override
    public List<ObjectActionParameter> getParameters(final Predicate<ObjectActionParameter> predicate) {
        final List<ObjectActionParameter> allParameters = getParameters();
        final List<ObjectActionParameter> selectedParameters = _Lists.newArrayList();
        for (int i = 0; i < allParameters.size(); i++) {
            if (predicate.test(allParameters.get(i))) {
                selectedParameters.add(allParameters.get(i));
            }
        }
        return selectedParameters;
    }

    ObjectActionParameter getParameter(final int position) {
        final List<ObjectActionParameter> parameters = getParameters();
        if (position >= parameters.size()) {
            throw new IllegalArgumentException("getParameter(int): only " + parameters.size() + " parameters, position=" + position);
        }
        return parameters.get(position);
    }



    // -- visable, usable

    @Override
    public VisibilityContext<?> createVisibleInteractionContext(
            final ObjectAdapter targetObjectAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new ActionVisibilityContext(targetObjectAdapter, this, getIdentifier(), interactionInitiatedBy, where);
    }

    @Override
    public UsabilityContext<?> createUsableInteractionContext(
            final ObjectAdapter targetObjectAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new ActionUsabilityContext(targetObjectAdapter, this, getIdentifier(), interactionInitiatedBy, where);
    }


    // -- validate

    @Override
    public Consent isProposedArgumentSetValid(
            final ObjectAdapter targetObject,
            final ObjectAdapter[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final InteractionResultSet resultSet = new InteractionResultSet();

        validateArgumentsIndividually(targetObject, proposedArguments, interactionInitiatedBy, resultSet);
        if (resultSet.isAllowed()) {
            // only check the action's own validity if all the arguments are OK.
            validateArgumentSet(targetObject, proposedArguments, interactionInitiatedBy, resultSet);
        }

        return resultSet.createConsent();
    }

    protected void validateArgumentsIndividually(
            final ObjectAdapter objectAdapter,
            final ObjectAdapter[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy,
            final InteractionResultSet resultSet) {
        final List<ObjectActionParameter> actionParameters = getParameters();
        if (proposedArguments != null) {
            for (int i = 0; i < proposedArguments.length; i++) {
                final ValidityContext<?> ic =
                        actionParameters.get(i).createProposedArgumentInteractionContext(
                                objectAdapter, proposedArguments, i, interactionInitiatedBy
                                );
                InteractionUtils.isValidResultSet(getParameter(i), ic, resultSet);
            }
        }
    }

    protected void validateArgumentSet(
            final ObjectAdapter objectAdapter,
            final ObjectAdapter[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy,
            final InteractionResultSet resultSet) {
        final ValidityContext<?> ic = createActionInvocationInteractionContext(
                objectAdapter, proposedArguments, interactionInitiatedBy);
        InteractionUtils.isValidResultSet(this, ic, resultSet);
    }

    ActionValidityContext createActionInvocationInteractionContext(
            final ObjectAdapter targetObject,
            final ObjectAdapter[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return new ActionValidityContext(targetObject, this, getIdentifier(), proposedArguments,
                interactionInitiatedBy);
    }



    // -- executeWithRuleChecking, execute

    @Override
    public ObjectAdapter executeWithRuleChecking(
            final ObjectAdapter target,
            final ObjectAdapter mixedInAdapter,
            final ObjectAdapter[] arguments,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {

        // see it?
        final Consent visibility = isVisible(target, interactionInitiatedBy, where);
        if (visibility.isVetoed()) {
            throw new HiddenException();
        }

        // use it?
        final Consent usability = isUsable(target, interactionInitiatedBy, where);
        if(usability.isVetoed()) {
            throw new DisabledException(usability.getReason());
        }

        // do it?
        final Consent validity = isProposedArgumentSetValid(target, arguments, interactionInitiatedBy);
        if(validity.isVetoed()) {
            throw new RecoverableException(validity.getReason());
        }

        return execute(target, mixedInAdapter, arguments, interactionInitiatedBy);
    }

    /**
     * Sets up the {@link Command}, then delegates off to
     * {@link #executeInternal(ObjectAdapter, ObjectAdapter, ObjectAdapter[], InteractionInitiatedBy) executeInternal}
     * to invoke the {@link ActionInvocationFacet invocation facet}.
     *
     * @param mixedInAdapter - will be null for regular actions, and for mixin actions.  When a mixin action invokes its underlying mixedIn action, then will be populated (so that the ActionDomainEvent can correctly provide the underlying mixin)
     */
    @Override
    public ObjectAdapter execute(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter mixedInAdapter,
            final ObjectAdapter[] argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        setupCommand(targetAdapter, argumentAdapters);

        return this.executeInternal(targetAdapter, mixedInAdapter, argumentAdapters, interactionInitiatedBy);
    }

    /**
     * private API, called by mixins and contributees.
     */
    public ObjectAdapter executeInternal(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter mixedInAdapter,
            final ObjectAdapter[] argumentAdapters, final InteractionInitiatedBy interactionInitiatedBy) {
        final ActionInvocationFacet facet = getFacet(ActionInvocationFacet.class);
        return facet.invoke(this, targetAdapter, mixedInAdapter, argumentAdapters, interactionInitiatedBy);
    }

    protected ActionInvocationFacet getActionInvocationFacet() {
        return getFacetedMethod().getFacet(ActionInvocationFacet.class);
    }




    // -- defaults

    @Override
    public ObjectAdapter[] getDefaults(final ObjectAdapter target) {

        final int parameterCount = getParameterCount();
        final List<ObjectActionParameter> parameters = getParameters();

        final Object[] parameterDefaultPojos;

        final ActionDefaultsFacet facet = getFacet(ActionDefaultsFacet.class);
        if (!facet.isNoop()) {
            // use the old defaultXxx approach
            parameterDefaultPojos = facet.getDefaults(target);
            if (parameterDefaultPojos.length != parameterCount) {
                throw new DomainModelException("Defaults array of incompatible size; expected " + parameterCount + " elements, but was " + parameterDefaultPojos.length + " for " + facet);
            }
            for (int i = 0; i < parameterCount; i++) {
                if (parameterDefaultPojos[i] != null) {
                    final ObjectSpecification componentSpec = getSpecificationLoader().loadSpecification(parameterDefaultPojos[i].getClass());
                    final ObjectSpecification parameterSpec = parameters.get(i).getSpecification();
                    if (!componentSpec.isOfType(parameterSpec)) {
                        throw new DomainModelException("Defaults type incompatible with parameter " + (i + 1) + " type; expected " + parameterSpec.getFullIdentifier() + ", but was " + componentSpec.getFullIdentifier());
                    }
                }
            }
        } else {
            // use the new defaultNXxx approach for each param in turn
            // (the reflector will have made sure both aren't installed).
            parameterDefaultPojos = new Object[parameterCount];
            for (int i = 0; i < parameterCount; i++) {
                final ActionParameterDefaultsFacet paramFacet = parameters.get(i).getFacet(ActionParameterDefaultsFacet.class);
                if (paramFacet != null && !paramFacet.isNoop()) {
                    parameterDefaultPojos[i] = paramFacet.getDefault(target, null);
                } else {
                    parameterDefaultPojos[i] = null;
                }
            }
        }

        final ObjectAdapter[] parameterDefaultAdapters = new ObjectAdapter[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            parameterDefaultAdapters[i] = adapterFor(parameterDefaultPojos[i]);
        }

        return parameterDefaultAdapters;
    }

    private ObjectAdapter adapterFor(final Object pojo) {
        return pojo == null ? null : getPersistenceSessionService().adapterFor(pojo);
    }

    private static ThreadLocal<List<ObjectAdapter>> commandTargetAdaptersHolder = new ThreadLocal<>();

    /**
     * A horrible hack to be able to persist a number of adapters in the command object.
     *
     * <p>
     *     What is really needed is to be able to invoke an action on a number of adapters all together.
     * </p>
     */
    public static <T> T withTargetAdapters(final List<ObjectAdapter> adapters, final Callable<T> callable) {
        commandTargetAdaptersHolder.set(adapters);
        try {
            return callable.call();
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            commandTargetAdaptersHolder.set(null);
        }
    }




    // -- choices

    @Override
    public ObjectAdapter[][] getChoices(
            final ObjectAdapter target,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final int parameterCount = getParameterCount();
        Object[][] parameterChoicesPojos;

        final ActionChoicesFacet facet = getFacet(ActionChoicesFacet.class);
        final List<ObjectActionParameter> parameters = getParameters();

        if (!facet.isNoop()) {
            // using the old choicesXxx() approach
            parameterChoicesPojos = facet.getChoices(target,
                    interactionInitiatedBy);

            // if no options, or not the right number of pojos, then default
            if (parameterChoicesPojos == null) {
                parameterChoicesPojos = new Object[parameterCount][];
            } else if (parameterChoicesPojos.length != parameterCount) {
                throw new DomainModelException(
                        String.format("Choices array of incompatible size; expected %d elements, but was %d for %s",
                                parameterCount, parameterChoicesPojos.length, facet));
            }
        } else {
            // use the new choicesNXxx approach for each param in turn
            // (the reflector will have made sure both aren't installed).

            parameterChoicesPojos = new Object[parameterCount][];
            for (int i = 0; i < parameterCount; i++) {
                final ActionParameterChoicesFacet paramFacet = parameters.get(i).getFacet(ActionParameterChoicesFacet.class);
                if (paramFacet != null && !paramFacet.isNoop()) {
                    parameterChoicesPojos[i] = paramFacet.getChoices(target, null,
                            interactionInitiatedBy);
                } else {
                    parameterChoicesPojos[i] = _Constants.emptyObjects;
                }
            }
        }

        final ObjectAdapter[][] parameterChoicesAdapters = new ObjectAdapter[parameterCount][];
        for (int i = 0; i < parameterCount; i++) {
            final ObjectSpecification paramSpec = parameters.get(i).getSpecification();

            if (parameterChoicesPojos[i] != null && parameterChoicesPojos[i].length > 0) {
                ObjectActionParameterAbstract.checkChoicesOrAutoCompleteType(
                        getSpecificationLoader(), parameterChoicesPojos[i], paramSpec);
                parameterChoicesAdapters[i] = new ObjectAdapter[parameterChoicesPojos[i].length];
                for (int j = 0; j < parameterChoicesPojos[i].length; j++) {
                    parameterChoicesAdapters[i][j] = adapterFor(parameterChoicesPojos[i][j]);
                }
            } else if (paramSpec.isNotCollection()) {
                parameterChoicesAdapters[i] = new ObjectAdapter[0];
            } else {
                throw new UnknownTypeException(paramSpec);
            }

            if (parameterChoicesAdapters[i].length == 0) {
                parameterChoicesAdapters[i] = null;
            }
        }

        return parameterChoicesAdapters;
    }


    /**
     * Internal API
     */
    @Override
    public void setupBulkActionInvocationContext(final ObjectAdapter targetAdapter) {

        final Object targetPojo = ObjectAdapter.Util.unwrap(targetAdapter);

        final BulkFacet bulkFacet = getFacetHolder().getFacet(BulkFacet.class);
        if (bulkFacet != null) {
            final org.apache.isis.applib.services.actinvoc.ActionInvocationContext actionInvocationContext = getActionInvocationContext();
            if (actionInvocationContext != null && actionInvocationContext.getInvokedOn() == null) {

                actionInvocationContext.setInvokedOn(InvokedOn.OBJECT);
                actionInvocationContext.setDomainObjects(Collections.singletonList(targetPojo));
            }
        }
    }

    @Override
    public boolean isPrototype() {
        return getType().isPrototype();
    }

    /**
     * Internal API, called by the various implementations of {@link ObjectAction} ({@link ObjectActionDefault default},
     * {@link ObjectActionMixedIn mixed-in} and {@link ObjectActionContributee contributee}).
     */
    public void setupCommand(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] argumentAdapters) {

        setupCommandTarget(targetAdapter, argumentAdapters);
        setupCommandMemberIdentifier();
        setupCommandMementoAndExecutionContext(targetAdapter, argumentAdapters);
    }

    private void setupCommandTarget(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] argumentAdapters) {

        final String arguments = CommandUtil.argDescriptionFor(this, argumentAdapters);
        setupCommandTarget(targetAdapter, arguments);
    }

    private void setupCommandMementoAndExecutionContext(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] argumentAdapters) {

        final CommandDtoServiceInternal commandDtoServiceInternal = getCommandDtoService();
        final List<ObjectAdapter> commandTargetAdapters =
                commandTargetAdaptersHolder.get() != null
                ? commandTargetAdaptersHolder.get()
                        : Collections.singletonList(targetAdapter);

                final CommandDto dto = commandDtoServiceInternal.asCommandDto(
                        commandTargetAdapters, this, argumentAdapters);

                setupCommandDtoAndExecutionContext(dto);

    }




    // -- toString

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("Action [");
        sb.append(super.toString());
        sb.append(",type=");
        sb.append(getType());
        sb.append(",returns=");
        sb.append(getReturnType());
        sb.append(",parameters={");
        for (int i = 0; i < getParameterCount(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(getParameters().get(i).getSpecification().getShortIdentifier());
        }
        sb.append("}]");
        return sb.toString();
    }



    // -- services (lookup)


    protected org.apache.isis.applib.services.actinvoc.ActionInvocationContext getActionInvocationContext() {
        return lookupService(org.apache.isis.applib.services.actinvoc.ActionInvocationContext.class);
    }




}
