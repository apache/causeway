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

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.exceptions.DomainModelException;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.CanVector;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.interactions.ActionUsabilityContext;
import org.apache.isis.core.metamodel.interactions.ActionValidityContext;
import org.apache.isis.core.metamodel.interactions.ActionVisibilityContext;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.NonNull;
import lombok.val;

public class ObjectActionDefault 
extends ObjectMemberAbstract 
implements ObjectAction {

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

    @Override
    public ActionInteractionHead interactionHead(@NonNull ManagedObject actionOwner) {
        return ActionInteractionHead.of(this, actionOwner, actionOwner);
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

    protected InteractionHead headFor(final ManagedObject target) {
        return InteractionHead.simple(target);
    }

    // -- visable, usable

    @Override
    public VisibilityContext createVisibleInteractionContext(
            final ManagedObject target, 
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return new ActionVisibilityContext(
                headFor(target), 
                this, 
                getIdentifier(), 
                interactionInitiatedBy, 
                where);
    }

    @Override
    public UsabilityContext createUsableInteractionContext(
            final ManagedObject target, 
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return new ActionUsabilityContext(
                headFor(target), 
                this, 
                getIdentifier(), 
                interactionInitiatedBy, 
                where);
    }


    // -- validate


    @Override
    public Consent isArgumentSetValid(
            final InteractionHead head,
            final Can<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final InteractionResultSet resultSet = new InteractionResultSet();

        validateArgumentsIndividually(head, proposedArguments, interactionInitiatedBy, resultSet);
        if (resultSet.isAllowed()) {
            // only check the action's own validity if all the arguments are OK.
            validateArgumentSet(head, proposedArguments, interactionInitiatedBy, resultSet);
        }

        return resultSet.createConsent();
    }


    @Override
    public InteractionResultSet isArgumentSetValidForParameters(
            final InteractionHead head,
            final Can<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final InteractionResultSet resultSet = new InteractionResultSet();

        validateArgumentsIndividually(head, proposedArguments, interactionInitiatedBy, resultSet);

        return resultSet;
    }

    private void validateArgumentsIndividually(
            final InteractionHead head,
            final Can<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy,
            final InteractionResultSet resultSet) {
        
        val actionParameters = getParameters();
        if (proposedArguments != null) {
            for (int i = 0; i < proposedArguments.size(); i++) {
                val validityContext = actionParameters.getElseFail(i)
                        .createProposedArgumentInteractionContext(
                                head, proposedArguments, i, interactionInitiatedBy);
                
                InteractionUtils.isValidResultSet(getParameter(i), validityContext, resultSet);
            }
        }
    }

    @Override
    public Consent isArgumentSetValidForAction(
            final InteractionHead head,
            final Can<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final InteractionResultSet resultSet = new InteractionResultSet();
        validateArgumentSet(head, proposedArguments, interactionInitiatedBy, resultSet);

        return resultSet.createConsent();
    }

    protected void validateArgumentSet(
            final InteractionHead head,
            final Can<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy,
            final InteractionResultSet resultSet) {
        
        val validityContext = createActionInvocationInteractionContext(
                head, proposedArguments, interactionInitiatedBy);
        InteractionUtils.isValidResultSet(this, validityContext, resultSet);
    }

    ActionValidityContext createActionInvocationInteractionContext(
            final InteractionHead head,
            final Can<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        return new ActionValidityContext(
                head, 
                this, 
                getIdentifier(), 
                proposedArguments,
                interactionInitiatedBy);
    }



    // -- executeWithRuleChecking, execute

    @Override
    public ManagedObject executeWithRuleChecking(
            final InteractionHead head,
            final Can<ManagedObject> arguments,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {

        val target = head.getOwner();
        
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
        final Consent validity = isArgumentSetValid(head, arguments, interactionInitiatedBy);
        if(validity.isVetoed()) {
            throw new RecoverableException(validity.getReason());
        }

        return execute(head, arguments, interactionInitiatedBy);
    }

    /**
     * Sets up the {@link Command}, then delegates to {@link #executeInternal(InteractionHead, Can, InteractionInitiatedBy)}
     * to invoke the {@link ActionInvocationFacet invocation facet}.
     */
    @Override
    public ManagedObject execute(
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        _Assert.assertEquals(this.getParameterCount(), argumentAdapters.size(),
                "action's parameter count and provided argument count must match");
        
        setupCommand(head.getTarget(), argumentAdapters);
        
        return this.executeInternal(head, argumentAdapters, interactionInitiatedBy);
    }

    /**
     * private API, called by mixins
     */
    protected ManagedObject executeInternal(
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        val actionInvocationFacet = getFacet(ActionInvocationFacet.class);
        return actionInvocationFacet
                .invoke(this, head, argumentAdapters, interactionInitiatedBy);
    }

    protected ActionInvocationFacet getActionInvocationFacet() {
        return getFacetedMethod().getFacet(ActionInvocationFacet.class);
    }

    // -- defaults

    @Override
    public Can<ManagedObject> getDefaults(final ManagedObject target) {

        val actionDefaultsFacet = getFacet(ActionDefaultsFacet.class);
        if (!actionDefaultsFacet.isFallback()) {
            
            // use the old defaultXxx approach
            
            final int parameterCount = getParameterCount();
            val parameters = getParameters();
            final Object[] parameterDefaultPojos;
            
            parameterDefaultPojos = actionDefaultsFacet.getDefaults(target);
            if (parameterDefaultPojos.length != parameterCount) {
                throw new DomainModelException("Defaults array of incompatible size; expected " + parameterCount + " elements, but was " + parameterDefaultPojos.length + " for " + actionDefaultsFacet);
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
            
            final ManagedObject[] parameterDefaultAdapters = new ManagedObject[parameterCount];
            for (int i = 0; i < parameterCount; i++) {
                val paramSpec = parameters.getElseFail(i).getSpecification();
                parameterDefaultAdapters[i] = ManagedObject.of(paramSpec, parameterDefaultPojos[i]);
            }

            return Can.ofArray(parameterDefaultAdapters);
            
        } 
        
        // else use the new defaultNXxx approach for each param in turn
        // (the reflector will have made sure both aren't installed).
        return interactionHead(target)
                .defaults()
                .getParamValues();

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
            throw new RecoverableException(e);
        } finally {
            commandTargetAdaptersHolder.remove();
        }
    }




    // -- choices

    @Override
    public CanVector<ManagedObject> getChoices(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final int parameterCount = getParameterCount();
        CanVector<ManagedObject> paramChoicesVector; 

        final ActionChoicesFacet facet = getFacet(ActionChoicesFacet.class);
        val parameters = getParameters();

        if (!facet.isFallback()) {
            // using the old choicesXxx() approach
            paramChoicesVector = facet.getChoices(target,
                    interactionInitiatedBy);

            // if no options, or not the right number of pojos, then default
            if (paramChoicesVector == null) {
                paramChoicesVector = new CanVector<>(parameterCount);
            } else if (paramChoicesVector.size() != parameterCount) {
                throw new DomainModelException(
                        String.format("Choices array of incompatible size; expected %d elements, but was %d for %s",
                                parameterCount, paramChoicesVector.size(), facet));
            }
        } else {
            // use the new choicesNXxx approach for each param in turn
            // (the reflector will have made sure both aren't installed).

            val emptyPendingArgs = Can.<ManagedObject>empty();
            paramChoicesVector = new CanVector<>(parameterCount);
            for (int i = 0; i < parameterCount; i++) {
                val param = parameters.getElseFail(i);
                val paramSpec = param.getSpecification();
                val paramFacet = param.getFacet(ActionParameterChoicesFacet.class);
                
                if (paramFacet != null && !paramFacet.isFallback()) {
                    val visibleChoices = paramFacet.getChoices(paramSpec, target, emptyPendingArgs, interactionInitiatedBy);
                    ObjectActionParameterAbstract.checkChoicesOrAutoCompleteType(
                            getSpecificationLoader(), visibleChoices, paramSpec);
                    paramChoicesVector.set(i, visibleChoices);
                } else {
                    paramChoicesVector.set(i, Can.empty());
                }
            }
        }
        return paramChoicesVector;

//        final List<Can<ManagedObject>> parameterChoicesAdapters = new ArrayList<>(parameterCount);
//        for (int i = 0; i < parameterCount; i++) {
//            
//            ManagedObject[] choices;
//            
//            final ObjectSpecification paramSpec = parameters.getElseFail(i).getSpecification();
//
//            if (paramChoicesVector[i] != null && paramChoicesVector[i].length > 0) {
//                ObjectActionParameterAbstract.checkChoicesOrAutoCompleteType(
//                        getSpecificationLoader(), paramChoicesVector[i], paramSpec);
//                choices = new ManagedObject[paramChoicesVector[i].length];
//                for (int j = 0; j < paramChoicesVector[i].length; j++) {
//                    choices[j] = ManagedObject.of(paramSpec, paramChoicesVector[i][j]);
//                }
//            } else if (paramSpec.isNotCollection()) {
//                choices = new ManagedObject[0];
//            } else {
//                throw new UnknownTypeException(paramSpec);
//            }
//
//            if (choices.length == 0) {
//                choices = null;
//            }
//            
//            parameterChoicesAdapters.add(Can.ofArray(choices));
//            
//        }
//
//        return Can.ofCollection(parameterChoicesAdapters);
    }

    @Override
    public boolean isPrototype() {
        return getType().isPrototype();
    }

    /**
     * Internal API, called by the various implementations of
     * {@link ObjectAction} ({@link ObjectActionDefault default} and
     * {@link ObjectActionMixedIn mixed-in}.
     */
    public void setupCommand(
            final ManagedObject targetAdapter,
            final Can<ManagedObject> argumentAdapters) {

        setupCommand(targetAdapter, uniqueId->commandDtoFor(uniqueId, targetAdapter, argumentAdapters));
    }

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

    // -- HELPER
    
    private CommandDto commandDtoFor(
            final UUID uniqueId,
            final ManagedObject targetAdapter,
            final Can<ManagedObject> argumentAdapters) {
        
        val commandTargetAdapters =
                commandTargetAdaptersHolder.get() != null
                    ? Can.ofCollection(commandTargetAdaptersHolder.get())
                    : Can.ofSingleton(targetAdapter);

        return getCommandDtoFactory()
                .asCommandDto(uniqueId, commandTargetAdapters, this, argumentAdapters);
    }
    
}
