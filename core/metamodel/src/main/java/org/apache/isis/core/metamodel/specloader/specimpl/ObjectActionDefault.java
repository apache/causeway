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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.commons.internal._Constants;
import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.core.commons.internal.collections._Lists;
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
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.DomainModelException;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.val;

public class ObjectActionDefault extends ObjectMemberAbstract implements ObjectAction {

    public static ActionType getType(final String typeStr) {
        final ActionType type = ActionType.valueOf(typeStr);
        if (type == null) {
            throw new IllegalArgumentException();
        }
        return type;
    }

    // -- fields

    private final _Lazy<Can<ObjectActionParameter>> parameters = _Lazy.threadSafe(this::determineParameters);

    // -- constructors

    public ObjectActionDefault(
            final FacetedMethod facetedMethod) {
        super(facetedMethod, FeatureType.ACTION);
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

    @Override
    public Can<ObjectActionParameter> getParameters() {
        return parameters.get();
    }

    protected Can<ObjectActionParameter> determineParameters() {
        
        val parameterCount = getParameterCount();
        val paramPeers = getFacetedMethod().getParameters();

        val parameters = _Lists.<ObjectActionParameter>newArrayList();
        for (int paramNum = 0; paramNum < parameterCount; paramNum++) {
            final FacetedMethodParameter paramPeer = paramPeers.get(paramNum);

            super.specificationOf(paramPeer.getType()); // preload

            // previously we threw an exception here if the specification represented a collection.  No longer!
            final ObjectActionParameter parameter =
                    paramPeer.getFeatureType() == FeatureType.ACTION_PARAMETER_SCALAR
                    ? new OneToOneActionParameterDefault(paramNum, this, paramPeer)
                            : new OneToManyActionParameterDefault(paramNum, this, paramPeer);

                    parameters.add(parameter);
        }
        return Can.ofCollection(parameters);
    }

    @Override
    public Can<ObjectSpecification> getParameterTypes() {
        val parameters = getParameters();
        val parameterTypes = parameters.map(ObjectActionParameter::getSpecification);
        return parameterTypes;
    }

    @Override
    public ObjectActionParameter getParameterById(final String paramId) {
        return getParameters().stream()
                .filter(param->Objects.equals(paramId, param.getId()))
                .findAny()
                .orElse(null);
    }

    @Override
    public ObjectActionParameter getParameterByName(final String paramName) {
        return getParameters().stream()
                .filter(param->Objects.equals(paramName, param.getName()))
                .findAny()
                .orElse(null);
    }

    @Override
    public Can<ObjectActionParameter> getParameters(final Predicate<ObjectActionParameter> filter) {
        return getParameters().filter(filter);
    }

    ObjectActionParameter getParameter(final int position) {
        val parameters = getParameters();
        if (position >= parameters.size()) {
            throw new IllegalArgumentException(
                    "getParameter(int): only " + parameters.size() + " parameters, position=" + position);
        }
        return parameters.getElseFail(position);
    }



    // -- visable, usable

    @Override
    public VisibilityContext<?> createVisibleInteractionContext(
            final ManagedObject targetObjectAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new ActionVisibilityContext(targetObjectAdapter, this, getIdentifier(), interactionInitiatedBy, where);
    }

    @Override
    public UsabilityContext<?> createUsableInteractionContext(
            final ManagedObject targetObjectAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new ActionUsabilityContext(targetObjectAdapter, this, getIdentifier(), interactionInitiatedBy, where);
    }


    // -- validate

    /**
     * The Validates all arguments individually (by calling same helper that
     * {@link #isEachIndividualArgumentValid(ManagedObject, List, InteractionInitiatedBy)} delegates to)
     * and if there are no validation errors, then validates the entire argument
     * set (by calling same helper that
     * {@link #isArgumentSetValid(ManagedObject, List, InteractionInitiatedBy)} delegates to).
     *
     * <p>
     * The two other validation methods mentioned above are separated out to allow viewers (such as the RO viewer) to
     * call the validation phases separately.
     * </p>
     */
    @Override
    public Consent isProposedArgumentSetValid(
            final ManagedObject targetObject,
            final Can<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final InteractionResultSet resultSet = new InteractionResultSet();

        validateArgumentsIndividually(targetObject, proposedArguments, interactionInitiatedBy, resultSet);
        if (resultSet.isAllowed()) {
            // only check the action's own validity if all the arguments are OK.
            validateArgumentSet(targetObject, proposedArguments, interactionInitiatedBy, resultSet);
        }

        return resultSet.createConsent();
    }

    /**
     * Normally action validation is all performed by
     * {@link #isProposedArgumentSetValid(ManagedObject, List, InteractionInitiatedBy)}, which calls
     * {@link #isEachIndividualArgumentValid(ManagedObject, List, InteractionInitiatedBy) this method} to
     * validate arguments individually, and then
     * {@link #isArgumentSetValid(ManagedObject, List, InteractionInitiatedBy) validate argument set}
     * afterwards.
     *
     * <p>
     * This method is in the API to allow viewers (eg the RO viewer) to call the different phases of validation
     * individually.
     * </p>
     */
    @Override
    public Consent isEachIndividualArgumentValid(
            final ManagedObject objectAdapter,
            final Can<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final InteractionResultSet resultSet = new InteractionResultSet();

        validateArgumentsIndividually(objectAdapter, proposedArguments, interactionInitiatedBy, resultSet);

        return resultSet.createConsent();
    }

    private void validateArgumentsIndividually(
            final ManagedObject objectAdapter,
            final Can<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy,
            final InteractionResultSet resultSet) {
        
        val actionParameters = getParameters();
        if (proposedArguments != null) {
            for (int i = 0; i < proposedArguments.size(); i++) {
                final ValidityContext<?> ic = actionParameters.getElseFail(i)
                        .createProposedArgumentInteractionContext(
                                objectAdapter, proposedArguments, i, interactionInitiatedBy);
                
                InteractionUtils.isValidResultSet(getParameter(i), ic, resultSet);
            }
        }
    }

    /**
     * Normally action validation is all performed by
     * {@link #isProposedArgumentSetValid(ManagedObject, List, InteractionInitiatedBy)}, which calls
     * {@link #isEachIndividualArgumentValid(ManagedObject, List, InteractionInitiatedBy)} to
     * validate arguments individually, and then
     * {@link #isArgumentSetValid(ManagedObject, List, InteractionInitiatedBy) this method} to
     * validate the entire argument set afterwards.
     *
     * <p>
     * This method is in the API to allow viewers (eg the RO viewer) to call the different phases of validation
     * individually.
     * </p>
     */
    @Override
    public Consent isArgumentSetValid(
            final ManagedObject objectAdapter,
            final Can<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final InteractionResultSet resultSet = new InteractionResultSet();
        validateArgumentSet(objectAdapter, proposedArguments, interactionInitiatedBy, resultSet);

        return resultSet.createConsent();
    }

    protected void validateArgumentSet(
            final ManagedObject objectAdapter,
            final Can<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy,
            final InteractionResultSet resultSet) {
        
        final ValidityContext<?> ic = createActionInvocationInteractionContext(
                objectAdapter, proposedArguments, interactionInitiatedBy);
        InteractionUtils.isValidResultSet(this, ic, resultSet);
    }

    ActionValidityContext createActionInvocationInteractionContext(
            final ManagedObject targetObject,
            final Can<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        return new ActionValidityContext(targetObject, this, getIdentifier(), proposedArguments,
                interactionInitiatedBy);
    }



    // -- executeWithRuleChecking, execute

    @Override
    public ManagedObject executeWithRuleChecking(
            final ManagedObject target,
            final ManagedObject mixedInAdapter,
            final Can<ManagedObject> arguments,
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
     * {@link #executeInternal(ManagedObject, ManagedObject, List, InteractionInitiatedBy) executeInternal}
     * to invoke the {@link ActionInvocationFacet invocation facet}.
     *
     * @param mixedInAdapter - will be null for regular actions, and for mixin actions.  When a mixin action invokes its underlying mixedIn action, then will be populated (so that the ActionDomainEvent can correctly provide the underlying mixin)
     */
    @Override
    public ManagedObject execute(
            final ManagedObject targetAdapter,
            final ManagedObject mixedInAdapter,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        setupCommand(targetAdapter, argumentAdapters);

        return this.executeInternal(targetAdapter, mixedInAdapter, argumentAdapters, interactionInitiatedBy);
    }

    /**
     * private API, called by mixins and contributees.
     */
    public ManagedObject executeInternal(
            final ManagedObject targetAdapter,
            final ManagedObject mixedInAdapter,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        val actionInvocationFacet = getFacet(ActionInvocationFacet.class);
        return actionInvocationFacet
                .invoke(this, targetAdapter, mixedInAdapter, argumentAdapters, interactionInitiatedBy);
    }

    protected ActionInvocationFacet getActionInvocationFacet() {
        return getFacetedMethod().getFacet(ActionInvocationFacet.class);
    }




    // -- defaults

    @Override
    public Can<ManagedObject> getDefaults(final ManagedObject target) {

        final int parameterCount = getParameterCount();
        val parameters = getParameters();

        final Object[] parameterDefaultPojos;

        final ActionDefaultsFacet facet = getFacet(ActionDefaultsFacet.class);
        if (!facet.isFallback()) {
            // use the old defaultXxx approach
            parameterDefaultPojos = facet.getDefaults(target);
            if (parameterDefaultPojos.length != parameterCount) {
                throw new DomainModelException("Defaults array of incompatible size; expected " + parameterCount + " elements, but was " + parameterDefaultPojos.length + " for " + facet);
            }
            for (int i = 0; i < parameterCount; i++) {
                if (parameterDefaultPojos[i] != null) {
                    final ObjectSpecification componentSpec = getSpecificationLoader().loadSpecification(parameterDefaultPojos[i].getClass());
                    final ObjectSpecification parameterSpec = parameters.getElseFail(i).getSpecification();
                    // TODO: should implement this instead as a MetaModelValidator
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
                final ActionParameterDefaultsFacet paramFacet = parameters.getElseFail(i)
                        .getFacet(ActionParameterDefaultsFacet.class);
                if (paramFacet != null && !paramFacet.isFallback()) {
                    parameterDefaultPojos[i] = paramFacet
                            .getDefault(
                                    target, 
                                    Can.empty(),
                                    null);
                } else {
                    parameterDefaultPojos[i] = null;
                }
            }
        }

        final ManagedObject[] parameterDefaultAdapters = new ManagedObject[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            val paramSpec = parameters.getElseFail(i).getSpecification();
            parameterDefaultAdapters[i] = ManagedObject.of(paramSpec, parameterDefaultPojos[i]);
        }

        return Can.ofArray(parameterDefaultAdapters);
    }

    private static ThreadLocal<List<ManagedObject>> commandTargetAdaptersHolder = new ThreadLocal<>();

    /**
     * A horrible hack to be able to persist a number of adapters in the command object.
     *
     * <p>
     *     What is really needed is to be able to invoke an action on a number of adapters all together.
     * </p>
     */
    public static <T> T withTargetAdapters(final List<ManagedObject> adapters, final Callable<T> callable) {
        commandTargetAdaptersHolder.set(adapters);
        try {
            return callable.call();
        } catch (Exception e) {
            throw new ApplicationException(e);
        } finally {
            commandTargetAdaptersHolder.remove();
        }
    }




    // -- choices

    @Override
    public Can<Can<ManagedObject>> getChoices(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final int parameterCount = getParameterCount();
        Object[][] parameterChoicesPojos;

        final ActionChoicesFacet facet = getFacet(ActionChoicesFacet.class);
        val parameters = getParameters();

        if (!facet.isFallback()) {
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
                final ActionParameterChoicesFacet paramFacet = parameters.getElseFail(i).getFacet(ActionParameterChoicesFacet.class);
                if (paramFacet != null && !paramFacet.isFallback()) {
                    parameterChoicesPojos[i] = paramFacet.getChoices(target, null,
                            interactionInitiatedBy);
                } else {
                    parameterChoicesPojos[i] = _Constants.emptyObjects;
                }
            }
        }

        final List<Can<ManagedObject>> parameterChoicesAdapters = new ArrayList<>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            
            ManagedObject[] choices;
            
            final ObjectSpecification paramSpec = parameters.getElseFail(i).getSpecification();

            if (parameterChoicesPojos[i] != null && parameterChoicesPojos[i].length > 0) {
                ObjectActionParameterAbstract.checkChoicesOrAutoCompleteType(
                        getSpecificationLoader(), parameterChoicesPojos[i], paramSpec);
                choices = new ManagedObject[parameterChoicesPojos[i].length];
                for (int j = 0; j < parameterChoicesPojos[i].length; j++) {
                    choices[j] = ManagedObject.of(paramSpec, parameterChoicesPojos[i][j]);
                }
            } else if (paramSpec.isNotCollection()) {
                choices = new ManagedObject[0];
            } else {
                throw new UnknownTypeException(paramSpec);
            }

            if (choices.length == 0) {
                choices = null;
            }
            
            parameterChoicesAdapters.add(Can.ofArray(choices));
            
        }

        return Can.ofCollection(parameterChoicesAdapters);
    }


    //    /**
    //     * Internal API
    //     */
    //    @Override
    //    public void setupBulkActionInvocationContext(final ObjectAdapter targetAdapter) {
    //
    //        final Object targetPojo = ObjectAdapter.Util.unwrap(targetAdapter);
    //
    //        final BulkFacet bulkFacet = getFacetHolder().getFacet(BulkFacet.class);
    //        if (bulkFacet != null) {
    //            final org.apache.isis.applib.services.actinvoc.ActionInvocationContext actionInvocationContext = getActionInvocationContext();
    //            if (actionInvocationContext != null && actionInvocationContext.getInvokedOn() == null) {
    //
    //                actionInvocationContext.setInvokedOn(InvokedOn.OBJECT);
    //                actionInvocationContext.setDomainObjects(Collections.singletonList(targetPojo));
    //            }
    //        }
    //    }

    @Override
    public boolean isPrototype() {
        return getType().isPrototype();
    }

    /**
     * Internal API, called by the various implementations of {@link ObjectAction} ({@link ObjectActionDefault default},
     * {@link ObjectActionMixedIn mixed-in} and {@link ObjectActionContributee contributee}).
     */
    public void setupCommand(
            final ManagedObject targetAdapter,
            final Can<ManagedObject> argumentAdapters) {

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        if (command.getExecutor() != Command.Executor.USER) {
            return;
        }

        setupCommandTarget(targetAdapter, argumentAdapters);
        setupCommandMemberIdentifier();
        setupCommandMementoAndExecutionContext(targetAdapter, argumentAdapters);
    }

    private void setupCommandTarget(
            final ManagedObject targetAdapter,
            final Can<ManagedObject> argumentAdapters) {

        final String arguments = CommandUtil.argDescriptionFor(this, argumentAdapters.toList());
        super.setupCommandTarget(targetAdapter, arguments);
    }

    private void setupCommandMementoAndExecutionContext(
            final ManagedObject targetAdapter,
            final Can<ManagedObject> argumentAdapters) {

        val commandDtoServiceInternal = getCommandDtoService();
        final List<ManagedObject> commandTargetAdapters =
                commandTargetAdaptersHolder.get() != null
                ? commandTargetAdaptersHolder.get()
                        : Collections.singletonList(targetAdapter);

                val commandDto = commandDtoServiceInternal.asCommandDto(
                        commandTargetAdapters, this, argumentAdapters.toList());

                setupCommandDtoAndExecutionContext(commandDto);

    }

    // -- toString

    @Override
    public ManagedObject realTargetAdapter(final ManagedObject targetAdapter) {
        return targetAdapter;
    }

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
            sb.append(getParameters().getElseFail(i).getSpecification().getShortIdentifier());
        }
        sb.append("}]");
        return sb.toString();
    }


    @Override
    public FacetHolder getFacetHolder() {
        return super.getFacetedMethod();
    }

}
