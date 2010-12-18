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


package org.apache.isis.core.metamodel.specloader.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.metamodel.adapter.Instance;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.exceptions.ModelException;
import org.apache.isis.core.metamodel.facets.actions.choices.ActionChoicesFacet;
import org.apache.isis.core.metamodel.facets.actions.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.facets.actions.executed.ExecutedFacet;
import org.apache.isis.core.metamodel.facets.actions.executed.ExecutedFacet.Where;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.interactions.ActionInvocationContext;
import org.apache.isis.core.metamodel.interactions.ActionUsabilityContext;
import org.apache.isis.core.metamodel.interactions.ActionVisibilityContext;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.MemberType;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.ObjectMemberAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationFacets;
import org.apache.isis.core.metamodel.spec.Target;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.specloader.internal.peer.ObjectActionParamPeer;
import org.apache.isis.core.metamodel.specloader.internal.peer.ObjectActionPeer;
import org.apache.log4j.Logger;


public class ObjectActionImpl extends ObjectMemberAbstract implements ObjectAction {
    private final static Logger LOG = Logger.getLogger(ObjectActionImpl.class);

    public static ObjectActionType getType(final String typeStr) {
    	ObjectActionType type = ObjectActionType.valueOf(typeStr);
    	if (type == null) {
    		throw new IllegalArgumentException();
    	} 
    	return type;
    }

    private final ObjectActionPeer objectActionPeer;
    /**
     * Lazily initialized by {@link #getParameters()} (so don't use directly!)
     */
    private ObjectActionParameter[] parameters;

    /**
     * Controls the initialization of {@link #contributed}.
     */
    private boolean knownWhetherContributed = false;
    /**
     * Lazily initialized, controlled by {@link #knownWhetherContributed}
     * 
     * @see #isContributed()
     */
    private boolean contributed;

    // //////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////

    public ObjectActionImpl(
    		final String methodId, 
    		final ObjectActionPeer objectActionPeer, 
    		final RuntimeContext runtimeContext) {
        super(methodId, objectActionPeer, MemberType.ACTION, runtimeContext);
        this.objectActionPeer = objectActionPeer;
    }

    // //////////////////////////////////////////////////////////////////
    // ReturnType, OnType, Actions (set)
    // //////////////////////////////////////////////////////////////////

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
     * Returns true if the represented action returns something, else returns false.
     */
    @Override
    public boolean hasReturn() {
        return getReturnType() != null;
    }

    @Override
    public ObjectSpecification getOnType() {
        final ActionInvocationFacet facet = getActionInvocationFacet();
        return facet.getOnType();
    }

    @Override
    public ObjectAction[] getActions() {
        return new ObjectAction[0];
    }

    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////
    
    @Override
    public Instance getInstance(ObjectAdapter adapter) {
        ObjectAction specification = this;
        return adapter.getInstance(specification);
    }

    // /////////////////////////////////////////////////////////////
    // Target, Type, IsContributed
    // /////////////////////////////////////////////////////////////

    @Override
    public Target getTarget() {
        final ExecutedFacet facet = getFacet(ExecutedFacet.class);
        final Where executeWhere = facet.value();
        if (executeWhere == Where.LOCALLY) {
            return Target.LOCAL;
        } else if (executeWhere == Where.REMOTELY) {
            return Target.REMOTE;
        } else if (executeWhere == Where.DEFAULT) {
            return Target.DEFAULT;
        } else {
            throw new UnknownTypeException(executeWhere);
        }
    }

    @Override
    public ObjectActionType getType() {
        return ObjectActionType.getType(this);
    }

    @Override
    public boolean isContributed() {
    	if (!knownWhetherContributed) {
    		contributed = determineWhetherContributed();
    		knownWhetherContributed = true;
    	}
        return contributed;
    }

	private boolean determineWhetherContributed() {
		if (getOnType().isService() && getParameterCount() > 0) {
            final ObjectActionParameter[] params = getParameters();
            for (int i = 0; i < params.length; i++) {
                if (params[i].isObject()) {
                    return true;
                }
            }
        }
        return false;
	}

    // /////////////////////////////////////////////////////////////
    // Parameters
    // /////////////////////////////////////////////////////////////

    @Override
    public int getParameterCount() {
        return objectActionPeer.getParameters().length;
    }

    @Override
    public boolean promptForParameters(final ObjectAdapter target) {
    	ObjectActionParameter[] parameters = getParameters();
        if (isContributed() && !target.getSpecification().isService()) {
            return getParameterCount() > 1 || !target.getSpecification().isOfType(parameters[0].getSpecification());
        } else {
            return getParameterCount() > 0;
        }
    }

    /**
     * Build lazily by {@link #getParameters()}.
     * 
     * <p>
     * Although this is lazily loaded, the method is also <tt>synchronized</tt> so there shouldn't be any
     * thread race conditions.
     */
    @Override
    public synchronized ObjectActionParameter[] getParameters() {
        if (this.parameters == null) {
            final int parameterCount = getParameterCount();
            final ObjectActionParameter[] parameters = new ObjectActionParameter[parameterCount];
            final ObjectActionParamPeer[] paramPeers = objectActionPeer.getParameters();
            for (int i = 0; i < parameterCount; i++) {
                final ObjectSpecification specification = paramPeers[i].getSpecification();
                if (specification.isParseable()) {
                    parameters[i] = new ObjectActionParameterParseable(i, this, paramPeers[i]);
                } else if (specification.isNotCollection()) {
                    parameters[i] = new OneToOneActionParameterImpl(i, this, paramPeers[i]);
                } else if (specification.isCollection()) {
                    throw new UnknownTypeException("collections not supported as parameters: " + getIdentifier());
                } else {
                    throw new UnknownTypeException(specification);
                }
            }
            this.parameters = parameters;
        }
        return parameters;
    }

    @Override
    public synchronized ObjectSpecification[] getParameterTypes() {
        List<ObjectSpecification> parameterTypes = new ArrayList<ObjectSpecification>();
        ObjectActionParameter[] parameters = getParameters();
        for(ObjectActionParameter parameter: parameters) {
            parameterTypes.add(parameter.getSpecification());
        }
        return parameterTypes.toArray(new ObjectSpecification[]{});
    }

    @Override
    public ObjectActionParameter[] getParameters(final Filter<ObjectActionParameter> filter) {
        final ObjectActionParameter[] allParameters = getParameters();
        final ObjectActionParameter[] selectedParameters = new ObjectActionParameter[allParameters.length];
        int v = 0;
        for (int i = 0; i < allParameters.length; i++) {
            if (filter.accept(allParameters[i])) {
                selectedParameters[v++] = allParameters[i];
            }
        }
        final ObjectActionParameter[] parameters = new ObjectActionParameter[v];
        System.arraycopy(selectedParameters, 0, parameters, 0, v);
        return parameters;
    }

    private ObjectActionParameter getParameter(final int position) {
        final ObjectActionParameter[] parameters = getParameters();
        if (position >= parameters.length) {
            throw new IllegalArgumentException("getParameter(int): only " + parameters.length + " parameters, position="
                    + position);
        }
        return parameters[position];
    }

    // /////////////////////////////////////////////////////////////
    // Visible (or hidden)
    // /////////////////////////////////////////////////////////////

    @Override
    public VisibilityContext<?> createVisibleInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObjectAdapter) {
        return new ActionVisibilityContext(session, invocationMethod, targetObjectAdapter, getIdentifier());
    }

    @Override
    public Consent isVisible(final AuthenticationSession session, final ObjectAdapter target) {
        return super.isVisible(session, realTarget(target));
    }

    // /////////////////////////////////////////////////////////////
    // Usable (or disabled)
    // /////////////////////////////////////////////////////////////

    @Override
    public UsabilityContext<?> createUsableInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObjectAdapter) {
        return new ActionUsabilityContext(session, invocationMethod, targetObjectAdapter, getIdentifier());
    }

    @Override
    public Consent isUsable(final AuthenticationSession session, final ObjectAdapter target) {
        return super.isUsable(session, realTarget(target));
    }

    // //////////////////////////////////////////////////////////////////
    // validate
    // //////////////////////////////////////////////////////////////////

    /**
     * TODO: currently this method is hard-coded to assume all interactions are initiated
     * {@link InteractionInvocationMethod#BY_USER by user}.
     */
    @Override
    public Consent isProposedArgumentSetValid(final ObjectAdapter object, final ObjectAdapter[] proposedArguments) {
        final ObjectAdapter[] parameters = realParameters(object, proposedArguments);
        return isProposedArgumentSetValidResultSet(realTarget(object), parameters).createConsent();
    }

    private InteractionResultSet isProposedArgumentSetValidResultSet(
            final ObjectAdapter object,
            final ObjectAdapter[] proposedArguments) {
        final InteractionInvocationMethod invocationMethod = InteractionInvocationMethod.BY_USER;

        final InteractionResultSet resultSet = new InteractionResultSet();
        final ObjectActionParameter[] actionParameters = getParameters();
        if (proposedArguments != null) {
            // TODO: doesn't seem to be used...
            // ObjectAdapter[] params = realParameters(object, proposedArguments);
            for (int i = 0; i < proposedArguments.length; i++) {
                final ValidityContext<?> ic = actionParameters[i].createProposedArgumentInteractionContext(getAuthenticationSession(),
                        invocationMethod, object, proposedArguments, i);
                InteractionUtils.isValidResultSet(getParameter(i), ic, resultSet);
            }
        }
        // only check the action's own validity if all the arguments are OK.
        if (resultSet.isAllowed()) {
            final ValidityContext<?> ic = createActionInvocationInteractionContext(getAuthenticationSession(), invocationMethod, object,
                    proposedArguments);
            InteractionUtils.isValidResultSet(this, ic, resultSet);
        }
        return resultSet;
    }

    @Override
    public ActionInvocationContext createActionInvocationInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObject,
            final ObjectAdapter[] proposedArguments) {
        return new ActionInvocationContext(getAuthenticationSession(), invocationMethod, targetObject, getIdentifier(), proposedArguments);
    }

    // //////////////////////////////////////////////////////////////////
    // execute
    // //////////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter execute(final ObjectAdapter object, final ObjectAdapter[] parameters) {
        LOG.debug("execute action " + object + "." + getId());
        final ObjectAdapter[] params = realParameters(object, parameters);
        final ObjectAdapter target = realTarget(object);
        final ActionInvocationFacet facet = getFacet(ActionInvocationFacet.class);
        return facet.invoke(target, params);
    }

    private ActionInvocationFacet getActionInvocationFacet() {
        return objectActionPeer.getFacet(ActionInvocationFacet.class);
    }

    /**
     * Previously (prior to 3.0.x) this method had a check to see if the action was on an instance. With the
     * reflector redesign this has been removed, because NOF 3.x only supports instance methods, not
     * class-level methods.
     */
    @Override
    public ObjectAdapter realTarget(final ObjectAdapter target) {
        if (target == null) {
            return findService();
        } else if (target.getSpecification().isService()) {
            return target;
        } else if (isContributed()) {
            return findService();
        } else {
            return target;
        }
    }

    private ObjectAdapter findService() {
        final List<ObjectAdapter> services = getRuntimeContext().getServices();
        for (ObjectAdapter serviceAdapter : services) {
            if (serviceAdapter.getSpecification() == getOnType()) {
                return serviceAdapter;
            }
        }
        throw new IsisException("failed to find service for action " + this.getName());
    }

    private ObjectAdapter[] realParameters(final ObjectAdapter target, final ObjectAdapter[] parameters) {
        if (parameters != null) {
            return parameters;
        }
        return isContributed() ? new ObjectAdapter[] { target } : new ObjectAdapter[0];
    }

    // //////////////////////////////////////////////////////////////////
    // defaults
    // //////////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter[] getDefaults(final ObjectAdapter target) {
        final ObjectAdapter realTarget = realTarget(target);

        final int parameterCount = getParameterCount();
        final ObjectActionParameter[] parameters = getParameters();

        final Object[] parameterDefaultPojos;

        // TODO here and elsewhere: the target needs to be
        // replaced by the service where the action is for a service!
        // set a flag on entry if for a service - or get from spec using isService
        final ActionDefaultsFacet facet = getFacet(ActionDefaultsFacet.class);
        if (!facet.isNoop()) {
        	// use the old defaultXxx approach
            parameterDefaultPojos = facet.getDefaults(realTarget);
            if (parameterDefaultPojos.length != parameterCount) {
                throw new ModelException("Defaults array of incompatible size; expected " + parameterCount + " elements, but was "
                        + parameterDefaultPojos.length + " for " + facet);
            } 
            for (int i = 0; i < parameterCount; i++) {
                if (parameterDefaultPojos[i] != null) {
                     ObjectSpecification componentSpec = getRuntimeContext().getSpecificationLoader().loadSpecification(
                            parameterDefaultPojos[i].getClass());
                    ObjectSpecification parameterSpec = parameters[i].getSpecification();
                    if (!componentSpec.isOfType(parameterSpec)) {
                        throw new ModelException("Defaults type incompatible with parameter " + (i + 1) + " type; expected "
                                + parameterSpec.getFullName() + ", but was " + componentSpec.getFullName());
                    }
                }
            }
        } else {
        	// use the new defaultNXxx approach for each param in turn
        	// (the reflector will have made sure both aren't installed).
        	parameterDefaultPojos = new Object[parameterCount];
            for (int i = 0; i < parameterCount; i++) {
            	ActionParameterDefaultsFacet paramFacet = parameters[i].getFacet(ActionParameterDefaultsFacet.class);
            	if (paramFacet != null && !paramFacet.isNoop()) {
            		parameterDefaultPojos[i] = paramFacet.getDefault(realTarget);
            	} else {
            		parameterDefaultPojos[i] = null;
            	}
            }
        }

        final ObjectAdapter[] parameterDefaultAdapters = new ObjectAdapter[parameterCount];
        if (parameterDefaultPojos != null) {
            for (int i = 0; i < parameterCount; i++) {
                parameterDefaultAdapters[i] = adapterFor(parameterDefaultPojos[i]);
            }
        }
        
        // set the target if contributed.
        if (isContributed() && target != null) {
            for (int i = 0; i < parameterCount; i++) {
                if (target.getSpecification().isOfType(parameters[i].getSpecification())) {
                    parameterDefaultAdapters[i] = target;
                }
            }
        }
        return parameterDefaultAdapters;
    }

    private ObjectAdapter adapterFor(final Object pojo) {
        return pojo == null ? null : getRuntimeContext().adapterFor(pojo);
    }


    // /////////////////////////////////////////////////////////////
    // options (choices)
    // /////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter[][] getChoices(final ObjectAdapter target) {
        final ObjectAdapter realTarget = realTarget(target);
        
        final int parameterCount = getParameterCount();
        Object[][] parameterChoicesPojos;
        
        final ActionChoicesFacet facet = getFacet(ActionChoicesFacet.class);
        ObjectActionParameter[] parameters = getParameters();
        
        if (!facet.isNoop()) {
            // using the old choicesXxx() approach
        	parameterChoicesPojos = facet.getChoices(realTarget);
        	
            // if no options, or not the right number of pojos, then default
            if (parameterChoicesPojos == null) {
                parameterChoicesPojos = new Object[parameterCount][];
            } else if (parameterChoicesPojos.length != parameterCount)  {
                throw new ModelException("Choices array of incompatible size; expected " + parameterCount + " elements, but was " + parameterChoicesPojos.length + " for " + facet);
            }
        } else {
        	// use the new choicesNXxx approach for each param in turn
        	// (the reflector will have made sure both aren't installed).
        	
        	parameterChoicesPojos = new Object[parameterCount][];
            for (int i = 0; i < parameterCount; i++) {
            	ActionParameterChoicesFacet paramFacet = parameters[i].getFacet(ActionParameterChoicesFacet.class);
            	if (paramFacet != null && !paramFacet.isNoop()) {
            		parameterChoicesPojos[i] = paramFacet.getChoices(realTarget);
            	} else {
            		parameterChoicesPojos[i] = new Object[0];
            	}
            }
        }


        final ObjectAdapter[][] parameterChoicesAdapters = new ObjectAdapter[parameterCount][];
        for (int i = 0; i < parameterCount; i++) {
            final ObjectSpecification paramSpec = parameters[i].getSpecification();

            if (parameterChoicesPojos[i] != null && parameterChoicesPojos[i].length > 0) {
                ObjectActionParameterAbstract.checkChoicesType(getRuntimeContext(), parameterChoicesPojos[i], paramSpec);
                parameterChoicesAdapters[i] = new ObjectAdapter[parameterChoicesPojos[i].length];
                for (int j = 0; j < parameterChoicesPojos[i].length; j++) {
                    parameterChoicesAdapters[i][j] = adapterFor(parameterChoicesPojos[i][j]);
                }
            } else if (SpecificationFacets.isBoundedSet(paramSpec)) {
                QueryFindAllInstances query = new QueryFindAllInstances(paramSpec.getFullName());
				final List<ObjectAdapter> allInstancesAdapter = getRuntimeContext().allMatchingQuery(query);
                parameterChoicesAdapters[i] = new ObjectAdapter[allInstancesAdapter.size()];
                int j = 0;
                for(ObjectAdapter adapter: allInstancesAdapter) {
                    parameterChoicesAdapters[i][j++] = adapter;
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


    // //////////////////////////////////////////////////////////////////
    // debug, toString
    // //////////////////////////////////////////////////////////////////

    @Override
    public String debugData() {
        final DebugString debugString = new DebugString();
        objectActionPeer.debugData(debugString);
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
            sb.append(getParameters()[i].getSpecification().getShortName());
        }
        sb.append("}]");
        return sb.toString();
    }






}
