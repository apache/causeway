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
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

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
    protected List<ObjectAssociation> fields;
    protected List<ObjectAction> objectActions;
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

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public String getIconName(final ObjectAdapter object) {
        return null;
    }

    @Override
    public boolean hasSubclasses() {
        return false;
    }

    @Override
    public ObjectSpecification[] interfaces() {
        return new ObjectSpecification[0];
    }

    @Override
    public ObjectSpecification[] subclasses() {
        return new ObjectSpecification[0];
    }

    @Override
    public ObjectSpecification superclass() {
        return superClassSpecification;
    }

    @Override
    public boolean isOfType(final ObjectSpecification specification) {
        return specification == this;
    }

    @Override
    public void addSubclass(final ObjectSpecification specification) {}

    @Override
    public Instance getInstance(ObjectAdapter adapter) {
        return adapter;
    }


    // //////////////////////////////////////////////////////////////////////
    // Introspection
    // //////////////////////////////////////////////////////////////////////

    protected void setIntrospected(boolean introspected) {
		this.introspected = introspected;
	}
    @Override
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

    @Override
    public Object getDefaultValue() {
        return null;
    }

    // //////////////////////////////////////////////////////////////////////
    // Identifier
    // //////////////////////////////////////////////////////////////////////

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    // //////////////////////////////////////////////////////////////////
    // create InteractionContext
    // //////////////////////////////////////////////////////////////////

    @Override
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

    @Override
    public List<ObjectAssociation> getAssociations() {
        return fields;
    }

    @Override
    public List<ObjectAssociation> getAssociations(final Filter<ObjectAssociation> filter) {
        final List<ObjectAssociation> allFields = getAssociations();

        final List<ObjectAssociation> selectedFields = Lists.newArrayList();
        for (int i = 0; i < allFields.size(); i++) {
            if (filter.accept(allFields.get(i))) {
                selectedFields.add(allFields.get(i));
            }
        }

        return selectedFields;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OneToOneAssociation> getProperties() {
        List<OneToOneAssociation> list = new ArrayList<OneToOneAssociation>();
        List associationList = getAssociations(ObjectAssociationFilters.PROPERTIES);
        list.addAll(associationList);
        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OneToManyAssociation> getCollections() {
        List<OneToManyAssociation> list = new ArrayList<OneToManyAssociation>();
        List associationList = getAssociations(ObjectAssociationFilters.COLLECTIONS);
        list.addAll(associationList);
        return list;
    }


    // //////////////////////////////////////////////////////////////////////
    // getObjectAction, getAction, getActions
    // //////////////////////////////////////////////////////////////////////

    protected List<ObjectAction> getActions(final List<ObjectAction> availableActions, final ObjectActionType type) {
        return Collections.emptyList();
    }

    @Override
    public List<ObjectAction> getObjectActions(final ObjectActionType... types) {
        List<ObjectAction> actions = new ArrayList<ObjectAction>();
        for(ObjectActionType type: types) {
            addActions(type, actions);
        }
        return actions;
    }

	private void addActions(ObjectActionType type,
			List<ObjectAction> actions) {
		if (!isService()) {
			actions.addAll(getServiceActions(type));
		}
		actions.addAll(getActions(objectActions, type));
	}

    @Override
    public List<ObjectAction> getServiceActionsFor(final ObjectActionType... types) {
        final List<ObjectAdapter> services = getRuntimeContext().getServices();
        final List<ObjectAction> relatedActions = new ArrayList<ObjectAction>();
            for (ObjectAdapter serviceAdapter : services) {
                final List<ObjectAction> matchingActions = new ArrayList<ObjectAction>();
            for (ObjectActionType type : types) {
                final List<ObjectAction> serviceActions = serviceAdapter.getSpecification().getObjectActions(type);
                for (int j = 0; j < serviceActions.size(); j++) {
                    final ObjectSpecification returnType = serviceActions.get(j).getReturnType();
                    if (returnType != null && returnType.isCollection()) {
                        final TypeOfFacet facet = serviceActions.get(j).getFacet(TypeOfFacet.class);
                        if (facet != null) {
                            final ObjectSpecification elementType = facet.valueSpec();
                            if (elementType.isOfType(this)) {
                                matchingActions.add(serviceActions.get(j));
                            }
                        }
                    } else if (returnType != null && returnType.isOfType(this)) {
                        matchingActions.add(serviceActions.get(j));
                    }
                }
            }
            if (matchingActions.size() > 0) {
                final ObjectActionSet set = new ObjectActionSet("id", serviceAdapter.titleString(), matchingActions,
                        runtimeContext);
                relatedActions.add(set);
            }
        }
        return relatedActions;
    }

    @Override
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

    @Override
    public boolean isService() {
        return false;
    }

    // //////////////////////////////////////////////////////////////////////
    // Dirty
    // //////////////////////////////////////////////////////////////////////

    @Override
    public boolean isDirty(final ObjectAdapter object) {
        return false;
    }

    @Override
    public void clearDirty(final ObjectAdapter object) {}

    @Override
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
    protected List<ObjectAction> getServiceActions(final ObjectActionType type) {
        if (isService()) {
            return Collections.emptyList();
        }
        final List<ObjectAdapter> services = getRuntimeContext().getServices();

        // will populate an ActionSet with all actions contributed by each service
        final List<ObjectAction> serviceActionSets = Lists.newArrayList();

        for (ObjectAdapter serviceAdapter : services) {
            final ObjectSpecification specification = serviceAdapter.getSpecification();
            if (specification == this) {
                continue;
            }

            final List<ObjectAction> serviceActions = specification.getObjectActions(type);
            final List<ObjectAction> matchingServiceActions = new ArrayList<ObjectAction>();
            for (int j = 0; j < serviceActions.size(); j++) {
                final ObjectAction serviceAction = serviceActions.get(j);
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
                final ObjectActionSet objectActionSet = new ObjectActionSet("id", serviceAdapter.titleString(),
                    matchingServiceActions, runtimeContext);
                serviceActionSets.add(objectActionSet);
            }

        }
        return serviceActionSets;
    }

    private boolean matchesParameterOf(final ObjectAction serviceAction) {
        final List<ObjectActionParameter> params = serviceAction.getParameters();
        for (int k = 0; k < params.size(); k++) {
            if ( isOfType(params.get(k).getSpecification())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Consent isValid(final ObjectAdapter inObject) {
        return isValidResult(inObject).createConsent();
    }

    /**
     * TODO: currently this method is hard-coded to assume all interactions are initiated
     * {@link InteractionInvocationMethod#BY_USER by user}.
     */
    @Override
    public InteractionResult isValidResult(final ObjectAdapter targetObjectAdapter) {
        final ObjectValidityContext validityContext = createValidityInteractionContext(getAuthenticationSession(),
                InteractionInvocationMethod.BY_USER, targetObjectAdapter);
        return InteractionUtils.isValidResult(this, validityContext);
    }

    /**
     * Create an {@link InteractionContext} representing an attempt to save the object.
     */
    @Override
    public ObjectValidityContext createValidityInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod interactionMethod,
            final ObjectAdapter targetObjectAdapter) {
        return new ObjectValidityContext(session, interactionMethod, targetObjectAdapter, getIdentifier());
    }

    @Override
    public Persistability persistability() {
        return Persistability.USER_PERSISTABLE;
    }

    // //////////////////////////////////////////////////////////////////////
    // convenience isXxx (looked up from facets)
    // //////////////////////////////////////////////////////////////////////

    @Override
    public boolean isParseable() {
        return containsFacet(ParseableFacet.class);
    }

    @Override
    public boolean isEncodeable() {
        return containsFacet(EncodableFacet.class);
    }

    @Override
    public boolean isValueOrIsAggregated() {
        return isValue() || isAggregated();
    }

    @Override
    public boolean isValue() {
        return containsFacet(ValueFacet.class);
    }

    @Override
    public boolean isAggregated() {
        return containsFacet(AggregatedFacet.class);
    }

    @Override
    public boolean isCollection() {
        return containsFacet(CollectionFacet.class);
    }

    @Override
    public boolean isNotCollection() {
        return !isCollection();
    }

    @Override
    public boolean isImmutable() {
        return containsFacet(ImmutableFacet.class);
    }

    @Override
    public boolean isHidden() {
        return containsFacet(HiddenFacet.class);
    }

    // //////////////////////////////////////////////////////////////////////
    // misc
    // //////////////////////////////////////////////////////////////////////

    @Override
    public boolean isCollectionOrIsAggregated() {
        return false;
    }

    @Override
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

