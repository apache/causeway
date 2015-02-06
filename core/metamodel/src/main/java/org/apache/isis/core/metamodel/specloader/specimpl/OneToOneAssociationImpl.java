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

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.init.PropertyInitializationFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.PropertyAccessContext;
import org.apache.isis.core.metamodel.interactions.PropertyModifyContext;
import org.apache.isis.core.metamodel.interactions.PropertyUsabilityContext;
import org.apache.isis.core.metamodel.interactions.PropertyVisibilityContext;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.facets.param.autocomplete.MinLengthUtil;

public class OneToOneAssociationImpl extends ObjectAssociationAbstract implements OneToOneAssociation {

    public OneToOneAssociationImpl(final FacetedMethod facetedMethod, final ObjectMemberContext objectMemberContext) {
        this(facetedMethod, getSpecification(objectMemberContext.getSpecificationLookup(), facetedMethod.getType()), objectMemberContext);
    }
    
    protected OneToOneAssociationImpl(final FacetedMethod facetedMethod, final ObjectSpecification objectSpec, final ObjectMemberContext objectMemberContext) {
        super(facetedMethod, FeatureType.PROPERTY, objectSpec, objectMemberContext);
    }

    // /////////////////////////////////////////////////////////////
    // Hidden (or visible)
    // /////////////////////////////////////////////////////////////

    @Override
    public VisibilityContext<?> createVisibleInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod invocationMethod, final ObjectAdapter ownerAdapter, Where where) {
        return new PropertyVisibilityContext(getDeploymentCategory(), session, invocationMethod, ownerAdapter, getIdentifier(), where);
    }

    // /////////////////////////////////////////////////////////////
    // Disabled (or enabled)
    // /////////////////////////////////////////////////////////////

    @Override
    public UsabilityContext<?> createUsableInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod invocationMethod, final ObjectAdapter ownerAdapter, Where where) {
        return new PropertyUsabilityContext(getDeploymentCategory(), session, invocationMethod, ownerAdapter, getIdentifier(), where);
    }

    // /////////////////////////////////////////////////////////////
    // Validate
    // /////////////////////////////////////////////////////////////

    @Override
    public ValidityContext<?> createValidateInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod interactionMethod, final ObjectAdapter ownerAdapter, final ObjectAdapter proposedToReferenceAdapter) {
        return new PropertyModifyContext(getDeploymentCategory(), session, interactionMethod, ownerAdapter, getIdentifier(), proposedToReferenceAdapter);
    }

    /**
     * TODO: currently this method is hard-coded to assume all interactions are
     * initiated {@link InteractionInvocationMethod#BY_USER by user}.
     */
    @Override
    public Consent isAssociationValid(final ObjectAdapter ownerAdapter, final ObjectAdapter proposedToReferenceAdapter) {
        return isAssociationValidResult(ownerAdapter, proposedToReferenceAdapter).createConsent();
    }

    private InteractionResult isAssociationValidResult(final ObjectAdapter ownerAdapter, final ObjectAdapter proposedToReferenceAdapter) {
        final ValidityContext<?> validityContext = createValidateInteractionContext(getAuthenticationSession(), InteractionInvocationMethod.BY_USER, ownerAdapter, proposedToReferenceAdapter);
        return InteractionUtils.isValidResult(this, validityContext);
    }

    // /////////////////////////////////////////////////////////////
    // init
    // /////////////////////////////////////////////////////////////

    @Override
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
        final PropertyOrCollectionAccessorFacet facet = getFacet(PropertyOrCollectionAccessorFacet.class);
        final Object referencedPojo = facet.getProperty(ownerAdapter);

        if (referencedPojo == null) {
            return null;
        }

        return getAdapterManager().adapterFor(referencedPojo, ownerAdapter);
    }

    /**
     * TODO: currently this method is hard-coded to assume all interactions are
     * initiated {@link InteractionInvocationMethod#BY_USER by user}.
     */
    @Override
    public PropertyAccessContext createAccessInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod interactionMethod, final ObjectAdapter ownerAdapter) {
        return new PropertyAccessContext(getDeploymentCategory(), session, InteractionInvocationMethod.BY_USER, ownerAdapter, getIdentifier(), get(ownerAdapter));
    }

    @Override
    public boolean isEmpty(final ObjectAdapter ownerAdapter) {
        return get(ownerAdapter) == null;
    }

    // /////////////////////////////////////////////////////////////
    // Set
    // /////////////////////////////////////////////////////////////

    @Override
    public void set(final ObjectAdapter ownerAdapter, final ObjectAdapter newReferencedAdapter) {
        if (newReferencedAdapter != null) {
            setValue(ownerAdapter, newReferencedAdapter);
        } else {
            clearValue(ownerAdapter);
        }
    }

    /**
     * @see #set(ObjectAdapter, ObjectAdapter)
     */
    @Deprecated
    @Override
    public void setAssociation(final ObjectAdapter ownerAdapter, final ObjectAdapter newReferencedAdapter) {
        setValue(ownerAdapter, newReferencedAdapter);
    }

    private void setValue(final ObjectAdapter ownerAdapter, final ObjectAdapter newReferencedAdapter) {
        final PropertySetterFacet setterFacet = getFacet(PropertySetterFacet.class);
        if (setterFacet == null) {
            return;
        }
        if (ownerAdapter.representsPersistent() && newReferencedAdapter != null && newReferencedAdapter.isTransient() && !newReferencedAdapter.getSpecification().isParented()) {
            // TODO: move to facet ?
            throw new IsisException("can't set a reference to a transient object from a persistent one: " + newReferencedAdapter.titleString() + " (transient)");
        }
        setterFacet.setProperty(ownerAdapter, newReferencedAdapter);
    }

    /**
     * @see #set(ObjectAdapter, ObjectAdapter)
     */
    @Deprecated
    @Override
    public void clearAssociation(final ObjectAdapter ownerAdapter) {
        clearValue(ownerAdapter);
    }

    private void clearValue(final ObjectAdapter ownerAdapter) {
        final PropertyClearFacet facet = getFacet(PropertyClearFacet.class);
        facet.clearProperty(ownerAdapter);
    }

    // /////////////////////////////////////////////////////////////
    // defaults
    // /////////////////////////////////////////////////////////////

    @Override
    public ObjectAdapter getDefault(final ObjectAdapter ownerAdapter) {
        PropertyDefaultFacet propertyDefaultFacet = getFacet(PropertyDefaultFacet.class);
        // if no default on the association, attempt to find a default on the
        // specification (eg an int should
        // default to 0).
        if (propertyDefaultFacet == null || propertyDefaultFacet.isNoop()) {
            propertyDefaultFacet = this.getSpecification().getFacet(PropertyDefaultFacet.class);
        }
        if (propertyDefaultFacet == null) {
            return null;
        }
        return propertyDefaultFacet.getDefault(ownerAdapter);
    }

    @Override
    public void toDefault(final ObjectAdapter ownerAdapter) {
        // don't default optional fields
        final MandatoryFacet mandatoryFacet = getFacet(MandatoryFacet.class);
        if (mandatoryFacet != null && mandatoryFacet.isInvertedSemantics()) {
            return;
        }

        final ObjectAdapter defaultValue = getDefault(ownerAdapter);
        if (defaultValue != null) {
            initAssociation(ownerAdapter, defaultValue);
        }
    }

    // /////////////////////////////////////////////////////////////
    // choices and autoComplete
    // /////////////////////////////////////////////////////////////

    @Override
    public boolean hasChoices() {
        return getFacet(PropertyChoicesFacet.class) != null;
    }

    @Override
    public ObjectAdapter[] getChoices(final ObjectAdapter ownerAdapter) {
        final PropertyChoicesFacet propertyChoicesFacet = getFacet(PropertyChoicesFacet.class);
        final Object[] pojoOptions = propertyChoicesFacet == null ? null : propertyChoicesFacet.getChoices(ownerAdapter, getSpecificationLookup());
        if (pojoOptions != null) {
            List<ObjectAdapter> adapters = Lists.transform(
                    Lists.newArrayList(pojoOptions), ObjectAdapter.Functions.adapterForUsing(getAdapterManager()));
            return adapters.toArray(new ObjectAdapter[]{});
        } 
        // // now incorporated into above choices processing (BoundedFacet is no more)
        /* else if (BoundedFacetUtils.isBoundedSet(getSpecification())) {
            return options();
        } */
        return null;
    }

    private <T> ObjectAdapter[] options() {
        final Query<T> query = new QueryFindAllInstances<T>(getSpecification().getFullIdentifier());
        final List<ObjectAdapter> allInstancesAdapter = getQuerySubmitter().allMatchingQuery(query);
        final ObjectAdapter[] options = new ObjectAdapter[allInstancesAdapter.size()];
        int j = 0;
        for (final ObjectAdapter adapter : allInstancesAdapter) {
            options[j++] = adapter;
        }
        return options;
    }

    
    @Override
    public boolean hasAutoComplete() {
        final PropertyAutoCompleteFacet propertyAutoCompleteFacet = getFacet(PropertyAutoCompleteFacet.class);
        return propertyAutoCompleteFacet != null;
    }

    @Override
    public ObjectAdapter[] getAutoComplete(ObjectAdapter ownerAdapter, String searchArg) {
        final PropertyAutoCompleteFacet propertyAutoCompleteFacet = getFacet(PropertyAutoCompleteFacet.class);
        final Object[] pojoOptions = propertyAutoCompleteFacet.autoComplete(ownerAdapter, searchArg);
        if (pojoOptions != null) {
            final ObjectAdapter[] options = new ObjectAdapter[pojoOptions.length];
            for (int i = 0; i < options.length; i++) {
                options[i] = getAdapterManager().adapterFor(pojoOptions[i]);
            }
            return options;
        }
        return null;
    }

    @Override
    public int getAutoCompleteMinLength() {
        final PropertyAutoCompleteFacet propertyAutoCompleteFacet = getFacet(PropertyAutoCompleteFacet.class);
        return propertyAutoCompleteFacet != null? propertyAutoCompleteFacet.getMinLength(): MinLengthUtil.MIN_LENGTH_DEFAULT;
    }


    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////

    @Override
    public Instance getInstance(final ObjectAdapter ownerAdapter) {
        final OneToOneAssociation specification = this;
        return ownerAdapter.getInstance(specification);
    }

    // /////////////////////////////////////////////////////////////
    // debug, toString
    // /////////////////////////////////////////////////////////////

    @Override
    public String debugData() {
        final DebugString debugString = new DebugString();
        debugString.indent();
        debugString.indent();
        getFacetedMethod().debugData(debugString);
        return debugString.toString();
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append(super.toString());
        str.setAddComma();
        str.append("persisted", !isNotPersisted());
        str.append("type", getSpecification().getShortIdentifier());
        return str.toString();
    }


}
