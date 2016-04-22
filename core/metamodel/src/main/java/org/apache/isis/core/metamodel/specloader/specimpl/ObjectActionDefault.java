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

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.InvokedOn;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.commons.debug.DebugString;
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
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.facets.actions.bulk.BulkFacet;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.debug.DebugFacet;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.actions.exploration.ExplorationFacet;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.interactions.ActionInvocationContext;
import org.apache.isis.core.metamodel.interactions.ActionUsabilityContext;
import org.apache.isis.core.metamodel.interactions.ActionVisibilityContext;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.services.command.CommandMementoService;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.DomainModelException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberDependencies;
import org.apache.isis.schema.cmd.v1.CommandMementoDto;
import org.apache.isis.schema.utils.CommandMementoDtoUtils;

public class ObjectActionDefault extends ObjectMemberAbstract implements ObjectAction {

    private final static Logger LOG = LoggerFactory.getLogger(ObjectActionDefault.class);

    public static ActionType getType(final String typeStr) {
        final ActionType type = ActionType.valueOf(typeStr);
        if (type == null) {
            throw new IllegalArgumentException();
        }
        return type;
    }

    //region > fields

    /**
     * Lazily initialized by {@link #getParameters()} (so don't use directly!)
     */
    private List<ObjectActionParameter> parameters;

    //endregion

    //region > constructors

    public ObjectActionDefault(
            final FacetedMethod facetedMethod,
            final ObjectMemberDependencies objectMemberDependencies) {
        super(facetedMethod, FeatureType.ACTION, objectMemberDependencies);
    }

    //endregion

    //region > ReturnType, OnType, Actions (set)
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
    public ActionSemantics.Of getSemantics() {
        final ActionSemanticsFacet facet = getFacet(ActionSemanticsFacet.class);
        return facet != null? facet.value(): ActionSemantics.Of.NON_IDEMPOTENT;
    }

    //endregion

    //region > Type
    @Override
    public ActionType getType() {
        return getType(this);
    }

    private static ActionType getType(final FacetHolder facetHolder) {
        Facet facet = facetHolder.getFacet(DebugFacet.class);
        if (facet != null) {
            return ActionType.DEBUG;
        }
        facet = facetHolder.getFacet(ExplorationFacet.class);
        if (facet != null) {
            return ActionType.EXPLORATION;
        }
        facet = facetHolder.getFacet(PrototypeFacet.class);
        if (facet != null) {
            return ActionType.PROTOTYPE;
        }
        return ActionType.USER;
    }
    //endregion

    //region > Parameters

    @Override
    public int getParameterCount() {
        return getFacetedMethod().getParameters().size();
    }

    @Override
    public boolean promptForParameters(final ObjectAdapter target) {
        return getParameterCount() != 0;
    }

    /**
     * Build lazily by {@link #getParameters()}.
     * 
     * <p>
     * Although this is lazily loaded, the method is also <tt>synchronized</tt>
     * so there shouldn't be any thread race conditions.
     */
    @Override
    public synchronized List<ObjectActionParameter> getParameters() {
        if (this.parameters == null) {
            final int parameterCount = getParameterCount();
            final List<ObjectActionParameter> parameters = Lists.newArrayList();
            final List<FacetedMethodParameter> paramPeers = getFacetedMethod().getParameters();
            for (int i = 0; i < parameterCount; i++) {
                final TypedHolder paramPeer = paramPeers.get(i);
                final ObjectSpecification specification = ObjectMemberAbstract.getSpecification(getSpecificationLoader(), paramPeer.getType());
                
                if (!specification.isNotCollection()) {
                    throw new UnknownTypeException("collections not supported as parameters: " + getIdentifier());
                }
                final ObjectActionParameter parameter = new OneToOneActionParameterDefault(i, this, paramPeer);
                parameters.add(parameter);
            }
            this.parameters = parameters;
        }
        return parameters;
    }

    @Override
    public synchronized List<ObjectSpecification> getParameterTypes() {
        final List<ObjectSpecification> parameterTypes = Lists.newArrayList();
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
    public List<ObjectActionParameter> getParameters(final Filter<ObjectActionParameter> filter) {
        final List<ObjectActionParameter> allParameters = getParameters();
        final List<ObjectActionParameter> selectedParameters = Lists.newArrayList();
        for (int i = 0; i < allParameters.size(); i++) {
            if (filter.accept(allParameters.get(i))) {
                selectedParameters.add(allParameters.get(i));
            }
        }
        return selectedParameters;
    }

    private ObjectActionParameter getParameter(final int position) {
        final List<ObjectActionParameter> parameters = getParameters();
        if (position >= parameters.size()) {
            throw new IllegalArgumentException("getParameter(int): only " + parameters.size() + " parameters, position=" + position);
        }
        return parameters.get(position);
    }

    //endregion

    //region > visable, usable

    @Override
    public VisibilityContext<?> createVisibleInteractionContext(
            final ObjectAdapter targetObjectAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new ActionVisibilityContext(targetObjectAdapter, getIdentifier(), interactionInitiatedBy, where);
    }

    @Override
    public UsabilityContext<?> createUsableInteractionContext(
            final ObjectAdapter targetObjectAdapter, final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        return new ActionUsabilityContext(targetObjectAdapter, getIdentifier(), interactionInitiatedBy, where);
    }
    //endregion

    //region > validate

    @Override
    public Consent isProposedArgumentSetValid(
            final ObjectAdapter target,
            final ObjectAdapter[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return isProposedArgumentSetValidResultSet(target, proposedArguments, interactionInitiatedBy).createConsent();
    }

    private InteractionResultSet isProposedArgumentSetValidResultSet(
            final ObjectAdapter objectAdapter,
            final ObjectAdapter[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final InteractionResultSet resultSet = new InteractionResultSet();
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
        // only check the action's own validity if all the arguments are OK.
        if (resultSet.isAllowed()) {
            final ValidityContext<?> ic = createActionInvocationInteractionContext(
                    objectAdapter, proposedArguments, interactionInitiatedBy);
            InteractionUtils.isValidResultSet(this, ic, resultSet);
        }
        return resultSet;
    }

    private ActionInvocationContext createActionInvocationInteractionContext(
            final ObjectAdapter targetObject,
            final ObjectAdapter[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return new ActionInvocationContext(targetObject, getIdentifier(), proposedArguments,
                interactionInitiatedBy);
    }

    //endregion

    //region > executeWithRuleChecking, execute

    @Override
    public ObjectAdapter executeWithRuleChecking(
            final ObjectAdapter target,
            final ObjectAdapter[] arguments,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {

        // see it?
        final Consent visibility = isVisible(target, interactionInitiatedBy, where);
        if (visibility.isVetoed()) {
            throw new AuthorizationException();
        }

        // use it?
        final Consent usability = isUsable(target, interactionInitiatedBy, where);
        if(usability.isVetoed()) {
            throw new AuthorizationException();
        }

        // do it?
        final Consent validity = isProposedArgumentSetValid(target, arguments, interactionInitiatedBy);
        if(validity.isVetoed()) {
            throw new RecoverableException(validity.getReason());
        }

        return execute(target, arguments, interactionInitiatedBy);
    }

    @Override
    public ObjectAdapter execute(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] arguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("execute action " + targetAdapter + "." + getId());
        }
        final ActionInvocationFacet facet = getFacet(ActionInvocationFacet.class);

        return facet.invoke(this, targetAdapter, arguments, interactionInitiatedBy);
    }

    protected ActionInvocationFacet getActionInvocationFacet() {
        return getFacetedMethod().getFacet(ActionInvocationFacet.class);
    }


    //endregion

    //region > defaults

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

    //endregion

    //region > choices

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
                    parameterChoicesPojos[i] = new Object[0];
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
    public void setupActionInvocationContext(final ObjectAdapter targetAdapter) {

        final Object targetPojo = unwrap(targetAdapter);

        final BulkFacet bulkFacet = getFacetHolder().getFacet(BulkFacet.class);
        if (bulkFacet != null) {
            final org.apache.isis.applib.services.actinvoc.ActionInvocationContext actionInvocationContext = getActionInvocationContext();
            if (actionInvocationContext != null && actionInvocationContext.getInvokedOn() == null) {

                actionInvocationContext.setInvokedOn(InvokedOn.OBJECT);
                actionInvocationContext.setDomainObjects(Collections.singletonList(targetPojo));
            }

            final Bulk.InteractionContext bulkInteractionContext = getBulkInteractionContext();

            if (bulkInteractionContext != null && bulkInteractionContext.getInvokedAs() == null) {

                bulkInteractionContext.setInvokedAs(Bulk.InteractionContext.InvokedAs.REGULAR);
                bulkInteractionContext.setDomainObjects(Collections.singletonList(targetPojo));
            }
        }
    }

    /**
     * Internal API
     */
    @Override
    public void setupCommand(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] arguments) {

        setupCommandTarget(targetAdapter, arguments);
        setupCommandMemberIdentifier();
        setupCommandMementoAndExecutionContext(targetAdapter, arguments);
    }


    protected void setupCommandTarget(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] arguments) {

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        if (command.getExecutor() != Command.Executor.USER) {
            return;
        }

        if(command.getTarget() != null) {
            // already set up by a ObjectActionContributee or edit form;
            // don't overwrite
            return;
        }

        command.setTargetClass(CommandUtil.targetClassNameFor(targetAdapter));
        command.setTargetAction(CommandUtil.targetActionNameFor(this));
        command.setArguments(CommandUtil.argDescriptionFor(this, arguments));

        final Bookmark targetBookmark = CommandUtil.bookmarkFor(targetAdapter);
        command.setTarget(targetBookmark);
    }

    protected void setupCommandMemberIdentifier() {

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        if (command.getExecutor() != Command.Executor.USER) {
            return;
        }

        if(Command.ACTION_IDENTIFIER_FOR_EDIT.equals(command.getMemberIdentifier())) {
            // special case for edit properties, don't overwrite
            return;
        }

        if (command.getMemberIdentifier() != null) {
            // any contributed/mixin actions will fire after the main action
            // the guard here prevents them from trashing the command's memberIdentifier
            return;
        }

        command.setMemberIdentifier(CommandUtil.actionIdentifierFor(this));
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

    protected void setupCommandMementoAndExecutionContext(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter[] arguments) {

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();


        if (command.getExecutor() != Command.Executor.USER) {
            return;
        }

        if(Command.ACTION_IDENTIFIER_FOR_EDIT.equals(command.getMemberIdentifier())) {
            // special case for edit properties, don't overwrite
            return;
        }

        if (command.getMemento() != null) {
            // guard here to prevent subsequent contributed/mixin actions from
            // trampling over the command's memento and execution context
            return;
        }

        // memento
        final CommandMementoService commandMementoService = getCommandMementoService();

        List<ObjectAdapter> commandTargetAdapters =
                commandTargetAdaptersHolder.get() != null
                        ? commandTargetAdaptersHolder.get()
                        : Collections.singletonList(targetAdapter);

        final CommandMementoDto dto = commandMementoService.asCommandMemento(
                commandTargetAdapters, this, arguments);

        final String mementoXml = CommandMementoDtoUtils.toXml(dto);
        command.setMemento(mementoXml);

        // copy over the command execution 'context' (if available)
        final CommandFacet commandFacet = getFacetHolder().getFacet(CommandFacet.class);
        if(commandFacet != null && !commandFacet.isDisabled()) {
            command.setExecuteIn(commandFacet.executeIn());
            command.setPersistence(commandFacet.persistence());
        } else {
            // if no facet, assume do want to execute right now, but only persist (eventually) if hinted.
            command.setExecuteIn(org.apache.isis.applib.annotation.Command.ExecuteIn.FOREGROUND);
            command.setPersistence(org.apache.isis.applib.annotation.Command.Persistence.IF_HINTED);
        }

    }

    //endregion

    //region > debug, toString

    @Override
    public String debugData() {
        final DebugString debugString = new DebugString();
        getFacetedMethod().debugData(debugString);
        return debugString.toString();
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
            sb.append(getParameters().get(i).getSpecification().getShortIdentifier());
        }
        sb.append("}]");
        return sb.toString();
    }

    //endregion


    private static Object unwrap(final ObjectAdapter adapter) {
        return adapter == null ? null : adapter.getObject();
    }

    private <T> T lookupService(final Class<T> serviceClass) {
        return getServicesInjector().lookupService(serviceClass);
    }

    protected CommandContext getCommandContext() {
        CommandContext commandContext = lookupService(CommandContext.class);
        if (commandContext == null) {
            throw new IllegalStateException("The CommandContext service is not registered!");
        }
        return commandContext;
    }

    protected CommandMementoService getCommandMementoService() {
        return lookupService(CommandMementoService.class);
    }

    protected Bulk.InteractionContext getBulkInteractionContext() {
        return lookupService(Bulk.InteractionContext.class);
    }

    protected org.apache.isis.applib.services.actinvoc.ActionInvocationContext getActionInvocationContext() {
        return lookupService(org.apache.isis.applib.services.actinvoc.ActionInvocationContext.class);
    }


}
