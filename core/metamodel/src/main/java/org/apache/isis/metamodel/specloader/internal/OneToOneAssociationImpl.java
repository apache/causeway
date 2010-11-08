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


package org.apache.isis.metamodel.specloader.internal;

import java.util.List;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.metamodel.adapter.Instance;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.metamodel.consent.InteractionResult;
import org.apache.isis.metamodel.facets.propcoll.access.PropertyAccessorFacet;
import org.apache.isis.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.metamodel.facets.properties.modify.PropertyInitializationFacet;
import org.apache.isis.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.metamodel.facets.propparam.validate.mandatory.MandatoryFacet;
import org.apache.isis.metamodel.interactions.InteractionUtils;
import org.apache.isis.metamodel.interactions.PropertyAccessContext;
import org.apache.isis.metamodel.interactions.PropertyModifyContext;
import org.apache.isis.metamodel.interactions.PropertyUsabilityContext;
import org.apache.isis.metamodel.interactions.PropertyVisibilityContext;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.ValidityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.runtimecontext.spec.feature.ObjectAssociationAbstract;
import org.apache.isis.metamodel.services.container.query.QueryFindAllInstances;
import org.apache.isis.metamodel.spec.SpecificationFacets;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.metamodel.specloader.internal.peer.ObjectAssociationPeer;


public class OneToOneAssociationImpl extends ObjectAssociationAbstract implements OneToOneAssociation {

    private final ObjectAssociationPeer associationPeer;

    public OneToOneAssociationImpl(
    		final ObjectAssociationPeer association, 
    		final RuntimeContext runtimeContext) {
        super(association.getIdentifier().getMemberName(), association.getSpecification(), MemberType.ONE_TO_ONE_ASSOCIATION, association, runtimeContext);
        this.associationPeer = association;
    }

    // /////////////////////////////////////////////////////////////
    // Hidden (or visible)
    // /////////////////////////////////////////////////////////////

    public VisibilityContext<?> createVisibleInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter ownerAdapter) {
        return new PropertyVisibilityContext(session, invocationMethod, ownerAdapter, getIdentifier());
    }

    // /////////////////////////////////////////////////////////////
    // Disabled (or enabled)
    // /////////////////////////////////////////////////////////////

    public UsabilityContext<?> createUsableInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter ownerAdapter) {
        return new PropertyUsabilityContext(session, invocationMethod, ownerAdapter, getIdentifier());
    }

    // /////////////////////////////////////////////////////////////
    // Validate
    // /////////////////////////////////////////////////////////////

    public ValidityContext<?> createValidateInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod interactionMethod,
            final ObjectAdapter ownerAdapter,
            final ObjectAdapter proposedToReferenceAdapter) {
        return new PropertyModifyContext(session, interactionMethod, ownerAdapter, getIdentifier(), proposedToReferenceAdapter);
    }

    /**
     * TODO: currently this method is hard-coded to assume all interactions are initiated
     * {@link InteractionInvocationMethod#BY_USER by user}.
     */
    public Consent isAssociationValid(final ObjectAdapter ownerAdapter, final ObjectAdapter proposedToReferenceAdapter) {
        return isAssociationValidResult(ownerAdapter, proposedToReferenceAdapter).createConsent();
    }

    private InteractionResult isAssociationValidResult(final ObjectAdapter ownerAdapter, final ObjectAdapter proposedToReferenceAdapter) {
        final ValidityContext<?> validityContext = createValidateInteractionContext(getAuthenticationSession(),
                InteractionInvocationMethod.BY_USER, ownerAdapter, proposedToReferenceAdapter);
        return InteractionUtils.isValidResult(this, validityContext);
    }

    // /////////////////////////////////////////////////////////////
    // init
    // /////////////////////////////////////////////////////////////

    public void initAssociation(final ObjectAdapter ownerAdapter, final ObjectAdapter referencedAdapter) {
        final PropertyInitializationFacet initializerFacet = getFacet(PropertyInitializationFacet.class);
        if (initializerFacet != null) {
            initializerFacet.initProperty(ownerAdapter, referencedAdapter);
        }
    }

    // /////////////////////////////////////////////////////////////
    // Access (get, isEmpty)
    // /////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter get(final ObjectAdapter ownerAdapter) {
        final PropertyAccessorFacet facet = getFacet(PropertyAccessorFacet.class);
        final Object referencedPojo = facet.getProperty(ownerAdapter);
        
        if (referencedPojo == null) {
            return null;
        }
        
        return getRuntimeContext().adapterFor(referencedPojo, ownerAdapter, this);
    }


    /**
     * TODO: currently this method is hard-coded to assume all interactions are initiated
     * {@link InteractionInvocationMethod#BY_USER by user}.
     */
    public PropertyAccessContext createAccessInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod interactionMethod,
            final ObjectAdapter ownerAdapter) {
        return new PropertyAccessContext(session, InteractionInvocationMethod.BY_USER, ownerAdapter, getIdentifier(),
                get(ownerAdapter));
    }

    @Override
    public boolean isEmpty(final ObjectAdapter ownerAdapter) {
        return get(ownerAdapter) == null;
    }

    // /////////////////////////////////////////////////////////////
    // Set
    // /////////////////////////////////////////////////////////////

    public void set(ObjectAdapter ownerAdapter, ObjectAdapter newReferencedAdapter) {
    	if (newReferencedAdapter != null) {
    		setAssociation(ownerAdapter, newReferencedAdapter);
    	} else {
    		clearAssociation(ownerAdapter);
    	}
    }

    public void setAssociation(final ObjectAdapter ownerAdapter, final ObjectAdapter newReferencedAdapter) {
        final PropertySetterFacet setterFacet = getFacet(PropertySetterFacet.class);
        if (setterFacet != null) {
            if (ownerAdapter.isPersistent() && 
			    newReferencedAdapter != null && 
			    newReferencedAdapter.isTransient()) {
            	// TODO: move to facet ?
			    throw new IsisException("can't set a reference to a transient object from a persistent one: "
			            + newReferencedAdapter.titleString() + " (transient)");
			}
            setterFacet.setProperty(ownerAdapter, newReferencedAdapter);
        }
    }

    public void clearAssociation(final ObjectAdapter ownerAdapter) {
        final PropertyClearFacet facet = getFacet(PropertyClearFacet.class);
        facet.clearProperty(ownerAdapter);
    }

    // /////////////////////////////////////////////////////////////
    // defaults
    // /////////////////////////////////////////////////////////////

    public ObjectAdapter getDefault(final ObjectAdapter ownerAdapter) {
        PropertyDefaultFacet propertyDefaultFacet = getFacet(PropertyDefaultFacet.class);
        // if no default on the association, attempt to find a default on the specification (eg an int should
        // default to 0).
        if (propertyDefaultFacet == null || propertyDefaultFacet.isNoop()) {
            propertyDefaultFacet = this.getSpecification().getFacet(PropertyDefaultFacet.class);
        }
        if (propertyDefaultFacet == null) {
            return null;
        }
        return propertyDefaultFacet.getDefault(ownerAdapter);
    }

    public void toDefault(final ObjectAdapter ownerAdapter) {
    	// don't default optional fields
    	MandatoryFacet mandatoryFacet = getFacet(MandatoryFacet.class);
    	if (mandatoryFacet != null && mandatoryFacet.isInvertedSemantics()) {
    		return;
    	}
    	
    	final ObjectAdapter defaultValue = (ObjectAdapter) getDefault(ownerAdapter);
        if (defaultValue != null) {
            initAssociation(ownerAdapter, defaultValue);
        }
    }

    // /////////////////////////////////////////////////////////////
    // options (choices)
    // /////////////////////////////////////////////////////////////

    @Override
    public boolean hasChoices() {
        final PropertyChoicesFacet propertyChoicesFacet = getFacet(PropertyChoicesFacet.class);
        final boolean optionEnabled = propertyChoicesFacet != null;
        return SpecificationFacets.isBoundedSet(getSpecification()) || optionEnabled;
    }

    public ObjectAdapter[] getChoices(final ObjectAdapter ownerAdapter) {
        final PropertyChoicesFacet propertyChoicesFacet = getFacet(PropertyChoicesFacet.class);
        final Object[] pojoOptions = propertyChoicesFacet == null ? null : propertyChoicesFacet.getChoices(ownerAdapter);
        if (pojoOptions != null) {
            final ObjectAdapter[] options = new ObjectAdapter[pojoOptions.length];
            for (int i = 0; i < options.length; i++) {
                options[i] = getRuntimeContext().adapterFor(pojoOptions[i]);
            }
            return options;
        } else if (SpecificationFacets.isBoundedSet(getSpecification())) {
        	
            QueryFindAllInstances query = new QueryFindAllInstances(getSpecification());
			final List<ObjectAdapter> allInstancesAdapter = getRuntimeContext().allMatchingQuery(query);
        	final ObjectAdapter[] options = new ObjectAdapter[allInstancesAdapter.size()];
        	int j = 0;
            for (ObjectAdapter adapter: allInstancesAdapter) {
            	options[j++] = adapter;
            }
            return options;
        }
        return null;
    }



    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////
    
    public Instance getInstance(ObjectAdapter ownerAdapter) {
        OneToOneAssociation specification = this;
        return ownerAdapter.getInstance(specification);
    }

    
    
    // /////////////////////////////////////////////////////////////
    // debug, toString
    // /////////////////////////////////////////////////////////////

    public String debugData() {
        final DebugString debugString = new DebugString();
        debugString.indent();
        debugString.indent();
        associationPeer.debugData(debugString);
        return debugString.toString();
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append(super.toString());
        str.setAddComma();
        str.append("persisted", !isNotPersisted());
        str.append("type", getSpecification().getShortName());
        return str.toString();
    }



}
