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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.CanVector;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.commons.UtilStr;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.InteractionResultSet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.causeway.core.metamodel.interactions.ActionUsabilityContext;
import org.apache.causeway.core.metamodel.interactions.ActionValidityContext;
import org.apache.causeway.core.metamodel.interactions.ActionVisibilityContext;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.InteractionUtils;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import lombok.Getter;
import lombok.NonNull;
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
        return new ObjectActionDefault(facetedMethod.getFeatureIdentifier(), facetedMethod, false, false);
    }

    public static ObjectAction forMixinMain(final FacetedMethod facetedMethod) {
        return new ObjectActionDefault(facetedMethod.getFeatureIdentifier(), facetedMethod, true, false);
    }

    @Override
    public String asciiId() {
        return getMetaModelContext().getAsciiIdentifierService().asciiIdFor(getId());
    }

    /**
     * JUnit Support
     */
    public static class forTesting {
        public static ObjectActionDefault forMethod(
                final FacetedMethod facetedMethod) {
            return new ObjectActionDefault(facetedMethod.getFeatureIdentifier(), facetedMethod, false, true);
        }
        public static ObjectAction forMixinMain(final FacetedMethod facetedMethod) {
            return new ObjectActionDefault(facetedMethod.getFeatureIdentifier(), facetedMethod, true, true);
        }
    }

    // -- CONSTRUCTION

    /**
     * For convenience, during introspection of mixin types,
     * the mixin's main is wrapped with an {@link ObjectActionDefault},
     * to ease further processing.
     * <p>
     * However, such an {@link ObjectActionDefault} then is never exposed by the meta-model.
     */
    @Getter(onMethod_ = @Override)
    private final boolean declaredOnMixin;

    private final boolean testing;

    protected ObjectActionDefault(
            final Identifier identifier,
            final FacetedMethod facetedMethod,
            final boolean declaredOnMixin,
            final boolean testing) {
        super(identifier, facetedMethod, FeatureType.ACTION);

        this.declaredOnMixin = declaredOnMixin;
        this.testing = testing;
    }

    // -- DECLARING TYPE

    @Getter(onMethod_ = @Override, lazy=true)
    private final ObjectSpecification declaringType = loadDeclaringType();

    private ObjectSpecification loadDeclaringType() {
        var declaringType = getActionInvocationFacet()
                .map(ActionInvocationFacet::getDeclaringType);
        // JUnit support
        if(testing
                && declaringType.isEmpty()) {
            return getSpecificationLoader().loadSpecification(getFacetedMethod().getMethod().getDeclaringClass());
        }
        return declaringType.orElseThrow(()->_Exceptions
                .illegalState("missing ActionInvocationFacet on action %s", getFeatureIdentifier()));
    }

    @Override
    public SemanticsOf getSemantics() {
        return lookupFacet(ActionSemanticsFacet.class)
        .map(ActionSemanticsFacet::value)
        .orElse(SemanticsOf.NON_IDEMPOTENT);
    }

    // -- ELEMENT TYPE

    @Getter(onMethod_ = @Override, lazy=true)
    private final ObjectSpecification elementType = loadElementType();

    private ObjectSpecification loadElementType() {
        return Facets.elementSpec(getFacetedMethod())
                .orElseGet(()->{
                    var returnType = getReturnType();
                    if(!returnType.isSingular()) {
                        log.warn("plural action return type requires a TypeOfFacet: {}", getFeatureIdentifier());
                    }
                    return returnType;
                });
    }

    // -- RETURN TYPE

    @Getter(onMethod_ = @Override, lazy=true)
    private final ObjectSpecification returnType = loadReturnType();

    private ObjectSpecification loadReturnType() {
        var returType = getActionInvocationFacet()
                .map(ActionInvocationFacet::getReturnType);
        // JUnit support
        if(testing
                && returType.isEmpty()) {
            return getSpecificationLoader().loadSpecification(getFacetedMethod().getMethod().getReturnType());
        }
        return returType.orElseThrow(()->_Exceptions
                .illegalState("framework bug: missing ActionInvocationFacet on action %s", getFeatureIdentifier()));
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
        return !getReturnType().isVoidPrimitive();
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

        var specLoader = getSpecificationLoader();

        return getFacetedMethod().getParameters()
        .map(facetedParam->{

            final int paramIndex = facetedParam.getParamIndex();
            var paramElementType = specLoader.loadSpecification(facetedParam.getType().elementType()); // preload

            return
                    facetedParam.getFeatureType() == FeatureType.ACTION_PARAMETER_SINGULAR
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
                .filter(param->Objects.equals(paramName, param.getCanonicalFriendlyName()))
                .findAny()
                .orElse(null);
    }

    @Override
    public Can<ObjectActionParameter> getParameters(final Predicate<ObjectActionParameter> filter) {
        return getParameters().filter(filter);
    }

    ObjectActionParameter getParameter(final int position) {
        var parameters = getParameters();
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
                where,
                InteractionUtils.renderPolicy(target));
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
                where,
                InteractionUtils.renderPolicy(target));
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

        var actionParameters = getParameters();
        if (proposedArguments != null) {
            for (int i = 0; i < proposedArguments.size(); i++) {
                var validityContext = actionParameters.getElseFail(i)
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

        var validityContext = createActionInvocationInteractionContext(
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

        var target = head.getOwner();

        // see it?
        final Consent visibility = isVisible(target, interactionInitiatedBy, where);
        if (visibility.isVetoed()) {
            throw new HiddenException();
        }

        // use it?
        final Consent usability = isUsable(target, interactionInitiatedBy, where);
        if(usability.isVetoed()) {
            throw new DisabledException(usability.getReasonAsString().orElse("no reason given"));
        }

        // do it?
        final Consent validity = isArgumentSetValid(head, arguments, interactionInitiatedBy);
        if(validity.isVetoed()) {
            throw new RecoverableException(validity.getReasonAsString().orElse("no reason given"));
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

        final ManagedObject owner = head.getOwner();

        if(!interactionInitiatedBy.isPassThrough()) {
            setupCommand(head, argumentAdapters);

            if(log.isInfoEnabled()) {
                Optional<Bookmark> bookmarkIfAny = owner.getBookmark();
                bookmarkIfAny.ifPresent(bookmark -> {   // should always be true
                    log.info("Executing: {}#{} {} {}",
                        getFeatureIdentifier().getLogicalTypeName(),
                        getFeatureIdentifier().getMemberLogicalName(),
                        UtilStr.entityAsStr(bookmark, getSpecificationLoader()),
                        argsFor(getParameters(), argumentAdapters));
                });
            }
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
        var actionInvocationFacet = getFacet(ActionInvocationFacet.class);
        return actionInvocationFacet
                .invoke(this, head, argumentAdapters, interactionInitiatedBy);
    }

    protected Optional<ActionInvocationFacet> getActionInvocationFacet() {
        return getFacetedMethod().lookupFacet(ActionInvocationFacet.class);
    }

    // -- choices

    @Override
    public CanVector<ManagedObject> getChoices(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final int parameterCount = getParameterCount();
        CanVector<ManagedObject> paramChoicesVector;

        var parameters = getParameters();

            // use the new choicesNXxx approach for each param in turn
            // (the reflector will have made sure both aren't installed).

        var emptyPendingArgs = Can.<ManagedObject>empty();
        paramChoicesVector = new CanVector<>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            var param = parameters.getElseFail(i);
            var paramSpec = param.getElementType();
            var paramFacet = param.getFacet(ActionParameterChoicesFacet.class);

            if (paramFacet != null && !paramFacet.getPrecedence().isFallback()) {

                var visibleChoices = paramFacet.getChoices(
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

    protected String argsFor(Can<ObjectActionParameter> parameters, Can<ManagedObject> arguments) {
        if(parameters.size() != arguments.size()) {
            return "???"; // shouldn't happen
        }
        return parameters.stream().map(IndexedFunction.zeroBased((i, param) -> {
            var id = param.getId();
            var argStr = argStr(id, arguments, i);
            return id + "=" + argStr;
        })).collect(Collectors.joining(","));
    }

    private static String argStr(
            final String paramId,
            final Can<ManagedObject> arguments,
            final int i) {
        return UtilStr.namedArgStr(paramId, arguments.get(i));
    }

    private CommandDto commandDtoFor(
            final UUID interactionId,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters) {

        return getCommandDtoFactory()
                .asCommandDto(interactionId, head, this, argumentAdapters);
    }

    private boolean calculateIsExplicitlyAnnotated() {
        var methodFacade = getFacetedMethod().getMethod();
        return methodFacade.synthesize(Action.class).isPresent()
                || methodFacade.synthesize(ActionLayout.class).isPresent();
    }

}
