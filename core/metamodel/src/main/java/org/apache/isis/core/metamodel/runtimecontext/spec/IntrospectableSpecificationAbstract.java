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


package org.apache.isis.core.metamodel.runtimecontext.spec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.Instance;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolderImpl;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.object.aggregated.AggregatedFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.interactions.InteractionContext;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.ObjectActionSet;
import org.apache.isis.core.metamodel.spec.IntrospectableSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;


// TODO work through all subclasses and remove duplicated overridden methods - this
// hierarchy is badly structured move common, and default, functionality to this class from subclasses.
public abstract class IntrospectableSpecificationAbstract
		extends FacetHolderImpl
		implements ObjectSpecification, IntrospectableSpecification {

	private boolean introspected = false;


    protected String fullName;
    protected ObjectAssociation[] fields;
    protected ObjectAction[] objectActions;
    protected ObjectSpecification superClassSpecification;
    protected Identifier identifier;

	private final RuntimeContext runtimeContext;

    // //////////////////////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////////////////////

    public IntrospectableSpecificationAbstract(
    		final RuntimeContext runtimeContext) {
    	this.runtimeContext = runtimeContext;
    }

    // //////////////////////////////////////////////////////////////////////
    // Class and stuff immediately derivable from class
    // //////////////////////////////////////////////////////////////////////

    public String getFullName() {
        return fullName;
    }

    public String getIconName(final ObjectAdapter object) {
        return null;
    }

    public boolean hasSubclasses() {
        return false;
    }

    public ObjectSpecification[] interfaces() {
        return new ObjectSpecification[0];
    }

    public ObjectSpecification[] subclasses() {
        return new ObjectSpecification[0];
    }

    public ObjectSpecification superclass() {
        return superClassSpecification;
    }

    public boolean isOfType(final ObjectSpecification specification) {
        return specification == this;
    }

    public void addSubclass(final ObjectSpecification specification) {}

    public Instance getInstance(ObjectAdapter adapter) {
        return adapter;
    }


    // //////////////////////////////////////////////////////////////////////
    // Introspection
    // //////////////////////////////////////////////////////////////////////

    protected void setIntrospected(boolean introspected) {
		this.introspected = introspected;
	}
    public boolean isIntrospected() {
    	return introspected;
    }

    // //////////////////////////////////////////////////////////////////////
    // Facet Handling
    // //////////////////////////////////////////////////////////////////////

    @Override
    public <Q extends Facet> Q getFacet(final Class<Q> facetType) {
        final Q facet = super.getFacet(facetType);
        Q noopFacet = null;
        if (isNotANoopFacet(facet)) {
            return facet;
        } else {
            noopFacet = facet;
        }
        if (interfaces() != null) {
        	final ObjectSpecification[] interfaces = interfaces();
        	for (int i = 0; i < interfaces.length; i++) {
        		final ObjectSpecification interfaceSpec = interfaces[i];
        		if (interfaceSpec == null) {
        			// HACK: shouldn't happen, but occurring on occasion when running
        			// XATs under JUnit4. Some sort of race condition?
        			continue;
        		}
        		final Q interfaceFacet = interfaceSpec.getFacet(facetType);
        		if (isNotANoopFacet(interfaceFacet)) {
        			return interfaceFacet;
        		} else {
        			if (noopFacet == null) {
        				noopFacet = interfaceFacet;
        			}
        		}
        	}
        }
        // search up the inheritance hierarchy
        ObjectSpecification superSpec = superclass();
        if (superSpec != null) {
        	Q superClassFacet = superSpec.getFacet(facetType);
        	if(isNotANoopFacet(superClassFacet)) {
        		return superClassFacet;
        	}
        }
    	return noopFacet;
    }

    private boolean isNotANoopFacet(final Facet facet) {
        return facet != null && !facet.isNoop();
    }

    // //////////////////////////////////////////////////////////////////////
    // DefaultValue
    // //////////////////////////////////////////////////////////////////////

    public Object getDefaultValue() {
        return null;
    }

    // //////////////////////////////////////////////////////////////////////
    // Identifier
    // //////////////////////////////////////////////////////////////////////

    public Identifier getIdentifier() {
        return identifier;
    }

    // //////////////////////////////////////////////////////////////////
    // create InteractionContext
    // //////////////////////////////////////////////////////////////////

    public ObjectTitleContext createTitleInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod interactionMethod,
            final ObjectAdapter targetObjectAdapter) {
        return new ObjectTitleContext(session, interactionMethod, targetObjectAdapter, getIdentifier(), targetObjectAdapter
                .titleString());
    }

    // //////////////////////////////////////////////////////////////////////
    // getStaticallyAvailableFields, getDynamically..Fields, getField
    // //////////////////////////////////////////////////////////////////////

    public ObjectAssociation[] getAssociations() {
        return fields;
    }

    public List<? extends ObjectAssociation> getAssociationList() {
        return Arrays.asList(fields);
    }

    public ObjectAssociation[] getAssociations(final Filter<ObjectAssociation> filter) {
        final ObjectAssociation[] allFields = getAssociations();

        final ObjectAssociation[] selectedFields = new ObjectAssociation[allFields.length];
        int v = 0;
        for (int i = 0; i < allFields.length; i++) {
            if (filter.accept(allFields[i])) {
                selectedFields[v++] = allFields[i];
            }
        }

        final ObjectAssociation[] fields = new ObjectAssociation[v];
        System.arraycopy(selectedFields, 0, fields, 0, v);
        return fields;
    }

    public List<? extends ObjectAssociation> getAssociationList(final Filter<ObjectAssociation> filter) {
        return Arrays.asList(getAssociations(filter));
    }

    @SuppressWarnings("unchecked")
    public List<OneToOneAssociation> getPropertyList() {
        List<OneToOneAssociation> list = new ArrayList<OneToOneAssociation>();
        List associationList = getAssociationList(ObjectAssociationFilters.PROPERTIES);
        list.addAll(associationList);
        return list;
    }

    @SuppressWarnings("unchecked")
    public List<OneToManyAssociation> getCollectionList() {
        List<OneToManyAssociation> list = new ArrayList<OneToManyAssociation>();
        List associationList = getAssociationList(ObjectAssociationFilters.COLLECTIONS);
        list.addAll(associationList);
        return list;
    }


    // //////////////////////////////////////////////////////////////////////
    // getObjectAction, getAction, getActions
    // //////////////////////////////////////////////////////////////////////

    protected ObjectAction[] getActions(final ObjectAction[] availableActions, final ObjectActionType type) {
        return new ObjectAction[0];
    }

    public ObjectAction[] getObjectActions(final ObjectActionType... types) {
    	List<? extends ObjectAction> actions = getObjectActionList(types);
		return actions.toArray(new ObjectAction[]{});
    }

	public List<? extends ObjectAction> getObjectActionList(final ObjectActionType... types) {
		List<ObjectAction> actions = new ArrayList<ObjectAction>();
    	for(ObjectActionType type: types) {
            addActions(type, actions);
    	}
		return actions;
	}

	private void addActions(ObjectActionType type,
			List<ObjectAction> actions) {
		if (!isService()) {
			actions.addAll(Arrays.asList(getServiceActions(type)));
		}
		actions.addAll(Arrays.asList(getActions(objectActions, type)));
	}

    public ObjectAction[] getServiceActionsFor(final ObjectActionType... types) {
        final List<ObjectAdapter> services = getRuntimeContext().getServices();
        final List<ObjectAction> relatedActions = new ArrayList<ObjectAction>();
            for (ObjectAdapter serviceAdapter : services) {
                final List<ObjectAction> matchingActions = new ArrayList<ObjectAction>();
            for (ObjectActionType type : types) {
                final ObjectAction[] serviceActions = serviceAdapter.getSpecification().getObjectActions(type);
                for (int j = 0; j < serviceActions.length; j++) {
                    final ObjectSpecification returnType = serviceActions[j].getReturnType();
                    if (returnType != null && returnType.isCollection()) {
                        final TypeOfFacet facet = serviceActions[j].getFacet(TypeOfFacet.class);
                        if (facet != null) {
                            final ObjectSpecification elementType = facet.valueSpec();
                            if (elementType.isOfType(this)) {
                                matchingActions.add(serviceActions[j]);
                            }
                        }
                    } else if (returnType != null && returnType.isOfType(this)) {
                        matchingActions.add(serviceActions[j]);
                    }
                }
            }
            if (matchingActions.size() > 0) {
                final ObjectActionSet set = new ObjectActionSet("id", serviceAdapter.titleString(), matchingActions,
                        runtimeContext);
                relatedActions.add(set);
            }
        }
        return (ObjectAction[]) relatedActions.toArray(new ObjectAction[relatedActions.size()]);
    }

    public boolean isAbstract() {
        return false;
    }

    /**
     * Whether or not this specification's class is marked as final, that is it may not have subclasses, and
     * hence methods that could be overridden.
     *
     * <p>
     * Note - not used at present.
     */
    public boolean isFinal() {
        return false;
    }

    public boolean isService() {
        return false;
    }

    // //////////////////////////////////////////////////////////////////////
    // Dirty
    // //////////////////////////////////////////////////////////////////////

    public boolean isDirty(final ObjectAdapter object) {
        return false;
    }

    public void clearDirty(final ObjectAdapter object) {}

    public void markDirty(final ObjectAdapter object) {}

    // //////////////////////////////////////////////////////////////////////
    // markAsService, findServiceMethodsWithParameter
    // //////////////////////////////////////////////////////////////////////

    /**
     * Finds all service actions of the specified type, if any.
     *
     * <p>
     * However, if this specification {@link #isService() is actually for} a service, then returns an empty
     * array.
     *
     * @return an array of {@link ObjectActionSet}s (!!), each of which contains
     *         {@link ObjectAction}s of the requested type.
     *
     */
    protected ObjectAction[] getServiceActions(final ObjectActionType type) {
        if (isService()) {
            return new ObjectAction[0];
        }
        final List<ObjectAdapter> services = getRuntimeContext().getServices();

        // will populate an ActionSet with all actions contributed by each service
        final List<ObjectActionSet> serviceActionSets = new ArrayList<ObjectActionSet>();

        for (ObjectAdapter serviceAdapter : services) {
            final ObjectSpecification specification = serviceAdapter.getSpecification();
            if (specification == this) {
                continue;
            }

            final ObjectAction[] serviceActions = specification.getObjectActions(type);
            final List<ObjectAction> matchingServiceActions = new ArrayList<ObjectAction>();
            for (int j = 0; j < serviceActions.length; j++) {
                final ObjectAction serviceAction = serviceActions[j];
                if (serviceAction.isAlwaysHidden()) {
                    // ignore if permanently hidden
                    continue;
                }
                // see if qualifies by inspecting all parameters
                if (matchesParameterOf(serviceAction)) {
                    matchingServiceActions.add(serviceAction);
                }
            }
            // only add if there are matching subactions.
            if (matchingServiceActions.size() > 0) {
                final ObjectAction[] asArray = matchingServiceActions.toArray(new ObjectAction[matchingServiceActions
                        .size()]);
                final ObjectActionSet objectActionSet = new ObjectActionSet("id", serviceAdapter.titleString(),
                    asArray, runtimeContext);
                serviceActionSets.add(objectActionSet);
            }

        }
        return serviceActionSets.toArray(new ObjectAction[] {});
    }

    private boolean matchesParameterOf(final ObjectAction serviceAction) {
        final ObjectActionParameter[] params = serviceAction.getParameters();
        for (int k = 0; k < params.length; k++) {
            if ( isOfType(params[k].getSpecification())) {
                return true;
            }
        }
        return false;
    }

    public Consent isValid(final ObjectAdapter inObject) {
        return isValidResult(inObject).createConsent();
    }

    /**
     * TODO: currently this method is hard-coded to assume all interactions are initiated
     * {@link InteractionInvocationMethod#BY_USER by user}.
     */
    public InteractionResult isValidResult(final ObjectAdapter targetObjectAdapter) {
        final ObjectValidityContext validityContext = createValidityInteractionContext(getAuthenticationSession(),
                InteractionInvocationMethod.BY_USER, targetObjectAdapter);
        return InteractionUtils.isValidResult(this, validityContext);
    }

    /**
     * Create an {@link InteractionContext} representing an attempt to save the object.
     */
    public ObjectValidityContext createValidityInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod interactionMethod,
            final ObjectAdapter targetObjectAdapter) {
        return new ObjectValidityContext(session, interactionMethod, targetObjectAdapter, getIdentifier());
    }

    public Persistability persistability() {
        return Persistability.USER_PERSISTABLE;
    }

    // //////////////////////////////////////////////////////////////////////
    // convenience isXxx (looked up from facets)
    // //////////////////////////////////////////////////////////////////////

    public boolean isParseable() {
        return containsFacet(ParseableFacet.class);
    }

    public boolean isEncodeable() {
        return containsFacet(EncodableFacet.class);
    }

    public boolean isValueOrIsAggregated() {
        return isValue() || isAggregated();
    }

    public boolean isValue() {
        return containsFacet(ValueFacet.class);
    }

    public boolean isAggregated() {
        return containsFacet(AggregatedFacet.class);
    }

    public boolean isCollection() {
        return containsFacet(CollectionFacet.class);
    }

    public boolean isNotCollection() {
        return !isCollection();
    }

    public boolean isImmutable() {
        return containsFacet(ImmutableFacet.class);
    }

    public boolean isHidden() {
        return containsFacet(HiddenFacet.class);
    }

    // //////////////////////////////////////////////////////////////////////
    // misc
    // //////////////////////////////////////////////////////////////////////

    public boolean isCollectionOrIsAggregated() {
        return false;
    }

    public Object createObject(CreationMode creationMode) {
        throw new UnsupportedOperationException(getFullName());
    }



    // //////////////////////////////////////////////////////////////////////
    // toString
    // //////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("class", fullName);
        return str.toString();
    }

    // //////////////////////////////////////////////////////////////////////
    // Dependencies (injected in constructor)
    // //////////////////////////////////////////////////////////////////////

    public RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    /**
     * Derived from {@link #getRuntimeContext() runtime context}.
     */
    protected final AuthenticationSession getAuthenticationSession() {
        return getRuntimeContext().getAuthenticationSession();
    }


}

