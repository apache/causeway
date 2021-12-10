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

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.exceptions.RecoverableException;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.CanVector;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.interactions.ActionUsabilityContext;
import org.apache.isis.core.metamodel.interactions.ActionValidityContext;
import org.apache.isis.core.metamodel.interactions.ActionVisibilityContext;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ObjectActionDefault
extends ObjectMemberAbstract
implements ObjectAction {

    public static ActionScope getType(final String typeStr) {
        final ActionScope type = ActionScope.valueOf(typeStr);
        if (type == null) {
            throw new IllegalArgumentException();
        }
        return type;
    }

    // -- FACTORIES

    public static ObjectActionDefault forMethod(
            final FacetedMethod facetedMethod) {
        return new ObjectActionDefault(facetedMethod.getFeatureIdentifier(), facetedMethod, false);
    }

    public static ObjectAction forMixinMain(final FacetedMethod facetedMethod) {
        return new ObjectActionDefault(facetedMethod.getFeatureIdentifier(), facetedMethod, true);
    }

    // -- CONSTRUCTION

    @Getter(onMethod_ = @Override)
    private final ObjectSpecification elementType;

    @Getter(onMethod_ = @Override)
    private final boolean declaredOnMixin;

    protected ObjectActionDefault(
            final Identifier identifier,
            final FacetedMethod facetedMethod,
            final boolean declaredOnMixin) {
        super(identifier, facetedMethod, FeatureType.ACTION);

        this.declaredOnMixin = declaredOnMixin;

        // In support of some JUnit tests, skip
        if(getActionInvocationFacet()==null) {
            elementType = null;
            return;
        }

        elementType = getFacetedMethod()
                .lookupFacet(TypeOfFacet.class)
                .map(TypeOfFacet::valueSpec)
                .orElseGet(()->{
                    val returnType = getReturnType();
                    if(!returnType.isNotCollection()) {
                        log.warn("non-scalar action return type requires a TypeOfFacet: %s", identifier);
                    }
                    return returnType;
                });
    }

    @Override
    public ObjectSpecification getReturnType() {
        return getActionInvocationFacet().getReturnType();
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
    public ObjectSpecification getDeclaringType() {
        return getActionInvocationFacet().getDeclaringType();
    }

    @Override
    public SemanticsOf getSemantics() {
        return lookupFacet(ActionSemanticsFacet.class)
        .map(ActionSemanticsFacet::value)
        .orElse(SemanticsOf.NON_IDEMPOTENT);
    }

    // -- TYPE

    @Override
    public ActionScope getScope() {
        return getScope(this);
    }

    private static ActionScope getScope(final FacetHolder facetHolder) {
        return facetHolder.containsFacet(PrototypeFacet.class)
            ? ActionScope.PROTOTYPE
            : ActionScope.PRODUCTION;
    }

    @Override
    public ActionInteractionHead interactionHead(
            final @NonNull ManagedObject actionOwner) {
        return ActionInteractionHead.of(this, actionOwner, actionOwner);
    }

    // -- Parameters

    private final _Lazy<Can<ObjectActionParameter>> parameters = _Lazy.threadSafe(this::determineParameters);

    @Override
    public int getParameterCount() {
        return getFacetedMethod().getParameters().size();
    }

    @Override
    public Can<ObjectActionParameter> getParameters() {
        return parameters.get();
    }

    protected Can<ObjectActionParameter> determineParameters() {

        val specLoader = getSpecificationLoader();

        return getFacetedMethod().getParameters()
        .map(facetedParam->{

            final int paramIndex = facetedParam.getParamIndex();
            val paramElementType = specLoader.loadSpecification(facetedParam.getType()); // preload

            return
                    facetedParam.getFeatureType() == FeatureType.ACTION_PARAMETER_SCALAR
                        ? new OneToOneActionParameterDefault(paramElementType, paramIndex, this)
                        : new OneToManyActionParameterDefault(paramElementType, paramIndex, this);

        });
    }

    @Override
    public Can<ObjectSpecification> getParameterTypes() {
        return getParameters().map(ObjectActionParameter::getElementType);
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
                .filter(param->Objects.equals(paramName, param.getStaticFriendlyName()
                        .orElseThrow(_Exceptions::unexpectedCodeReach)))
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

    // -- VISIBLE

    @Override
    public VisibilityContext createVisibleInteractionContext(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return new ActionVisibilityContext(
                headFor(target),
                this,
                getFeatureIdentifier(),
                interactionInitiatedBy,
                where);
    }

    // -- USABLE

    @Override
    public UsabilityContext createUsableInteractionContext(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {
        return new ActionUsabilityContext(
                headFor(target),
                this,
                getFeatureIdentifier(),
                interactionInitiatedBy,
                where);
    }

    // -- VALIDATE

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
                getFeatureIdentifier(),
                proposedArguments,
                interactionInitiatedBy);
    }

    // -- EXECUTE

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

        if(!interactionInitiatedBy.isPassThrough()) {
            setupCommand(head, argumentAdapters);
        }

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

    // -- choices

    @Override
    public CanVector<ManagedObject> getChoices(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final int parameterCount = getParameterCount();
        CanVector<ManagedObject> paramChoicesVector;

        val parameters = getParameters();

            // use the new choicesNXxx approach for each param in turn
            // (the reflector will have made sure both aren't installed).

        val emptyPendingArgs = Can.<ManagedObject>empty();
        paramChoicesVector = new CanVector<>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            val param = parameters.getElseFail(i);
            val paramSpec = param.getElementType();
            val paramFacet = param.getFacet(ActionParameterChoicesFacet.class);

            if (paramFacet != null && !paramFacet.getPrecedence().isFallback()) {

                val visibleChoices = paramFacet.getChoices(
                        paramSpec,
                        interactionHead(target),
                        emptyPendingArgs,
                        interactionInitiatedBy);
                ObjectActionParameterAbstract.checkChoicesOrAutoCompleteType(
                        getSpecificationLoader(), visibleChoices, paramSpec);
                paramChoicesVector.set(i, visibleChoices);
            } else {
                paramChoicesVector.set(i, Can.empty());
            }
        }
        return paramChoicesVector;
    }

    @Override
    public boolean isPrototype() {
        return getScope().isPrototype();
    }

    @Getter(lazy=true, onMethod_ = {@Override})
    private final boolean explicitlyAnnotated = calculateIsExplicitlyAnnotated();

    /**
     * Internal API, called by the various implementations of
     * {@link ObjectAction} ({@link ObjectActionDefault default} and
     * {@link ObjectActionMixedIn mixed-in}.
     */
    public void setupCommand(
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters) {

        if(head.getOwner().getSpecification().isValue()) {
            return; // do not record value type mixin actions
        }

        setupCommand(head,
                interactionId->commandDtoFor(interactionId, head, argumentAdapters));
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
        sb.append(",scope=");
        sb.append(getScope());
        sb.append(",returns=");
        sb.append(getReturnType());
        sb.append(",parameters={");
        for (int i = 0; i < getParameterCount(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(getParameters().getElseFail(i).getElementType().getShortIdentifier());
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
            final UUID interactionId,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters) {

        return getCommandDtoFactory()
                .asCommandDto(interactionId, Can.ofSingleton(head), this, argumentAdapters);
    }

    private boolean calculateIsExplicitlyAnnotated() {
        val javaMethod = getFacetedMethod().getMethod();
        return _Annotations.synthesizeInherited(javaMethod, Action.class).isPresent()
                || _Annotations.synthesizeInherited(javaMethod, ActionLayout.class).isPresent();
    }

}
