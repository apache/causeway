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

package org.apache.isis.core.metamodel.testspec;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState;

import lombok.val;

public class ObjectSpecificationStub extends FacetHolderImpl implements ObjectSpecification {

    private ObjectAction action;
    public List<ObjectAssociation> fields = _Lists.newArrayList();
    private final String name;
    private Set<ObjectSpecification> subclasses = Collections.emptySet();
    private String title;
    /**
     * lazily derived, see {@link #getSpecId()} 
     */
    private ObjectSpecId specId;

    private ObjectSpecification elementSpecification;

    public ObjectSpecificationStub(final Class<?> type) {
        this(type.getName());
    }

    @Override
    public Optional<? extends ObjectMember> getMember(final String memberId) {
        val objectAction = getObjectAction(memberId);
        if(objectAction.isPresent()) {
            return objectAction;
        }
        val association = getAssociation(memberId);
        if(association.isPresent()) {
            return association;
        }
        return Optional.empty();
    }

    @Override
    public Class<?> getCorrespondingClass() {
        try {
            return Class.forName(name);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectSpecificationStub(final String name) {
        this.name = name;
        title = "";
    }

    @Override
    public FeatureType getFeatureType() {
        return FeatureType.OBJECT;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public Optional<ObjectAssociation> getAssociation(final String name) {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getId().equals(name)) {
                return Optional.ofNullable(fields.get(i));
            }
        }
        return Optional.empty();
    }

    @Override
    public Stream<ObjectAssociation> streamAssociations(final MixedIn contributed) {
        return fields.stream();
    }

    @Override
    public String getFullIdentifier() {
        return name;
    }

    @Override
    public ObjectSpecId getSpecId() {
        if(specId == null) {
            specId = getFacet(ObjectSpecIdFacet.class).value();
        }
        return specId;
    }

    @Override
    public String getIconName(final ManagedObject reference) {
        return null;
    }

    @Override
    public Object getNavigableParent(Object object) {
        return null;
    }

    @Override
    public String getCssClass(final ManagedObject reference) {
        return null;
    }

    private Optional<ObjectAction> lookupObjectAction(final String name) {
        if (action != null && action.getId().equals(name)) {
            return Optional.of(action);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ObjectAction> getObjectAction(final String id, final ActionType type) {
        val nameParmsIdentityString = id.substring(0, id.indexOf('('));
        val action = lookupObjectAction(nameParmsIdentityString);
        
        if(type==null) {
            return action;
        }
        
        if (action.isPresent()
                && action.get().getType() == type) {
            return action;
        }
        return Optional.empty();
        
    }

    @Override
    public String getPluralName() {
        return null;
    }

    @Override
    public String getShortIdentifier() {
        return name.substring(name.lastIndexOf('.') + 1);
    }

    @Override
    public String getSingularName() {
        return name + " (singular)";
    }

    @Override
    public String getDescription() {
        return getSingularName();
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String getTitle(
            final ManagedObject contextAdapterIfAny,
            final ManagedObject targetAdapter) {
        return title;
    }

    @Override
    public boolean hasSubclasses() {
        return false;
    }

    @Override
    public List<ObjectSpecification> interfaces() {
        return Collections.emptyList();
    }

    @Override
    public boolean isOfType(final ObjectSpecification cls) {
        return cls == this;
    }

    @Override
    public boolean isEncodeable() {
        return false;
    }

    @Override
    public boolean isParseable() {
        return false;
    }

    @Override
    public boolean isParented() {
        return false;
    }

    @Override
    public Set<ObjectSpecification> subclasses(final Depth depth) {
        return subclasses;
    }

    @Override
    public ObjectSpecification superclass() {
        return null;
    }

    @Override
    public Consent isValid(final ManagedObject targetAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        return null;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public Identifier getIdentifier() {
        return Identifier.classIdentifier(name);
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isImmutable() {
        return containsFacet(ImmutableFacet.class);
    }

    @Override
    public ObjectValidityContext createValidityInteractionContext(
            ManagedObject targetAdapter, InteractionInitiatedBy interactionInitiatedBy) {
        return null;
    }

    @Override
    public ObjectTitleContext createTitleInteractionContext(
            ManagedObject targetObjectAdapter, InteractionInitiatedBy invocationMethod) {
        return null;
    }

    @Override
    public InteractionResult isValidResult(ManagedObject targetAdapter, InteractionInitiatedBy interactionInitiatedBy) {
        return null;
    }


    // /////////////////////////////////////////////////////////////
    // introspection
    // /////////////////////////////////////////////////////////////

    @Override
    public Stream<ObjectAction> streamObjectActions(final MixedIn contributed) {
        return null;
    }

    @Override
    public Stream<ObjectAction> streamObjectActions(ImmutableEnumSet<ActionType> types, MixedIn contributed) {
        return null;
    }
    
    // /////////////////////////////////////////////////////////
    // view models and wizards
    // /////////////////////////////////////////////////////////

    @Override
    public boolean isViewModelCloneable(final ManagedObject targetAdapter) {
        return false;
    }

    @Override
    public boolean isWizard() {
        return false;
    }

    @Override
    public String toString() {
        return getFullIdentifier();
    }

    @Override
    public Optional<ObjectSpecification> getElementSpecification() {
        return Optional.ofNullable(elementSpecification);
    }

    @Override
    public BeanSort getBeanSort() {
        return BeanSort.UNKNOWN; // [2158] not implemented yet
    }

    @Override
    public void introspectUpTo(IntrospectionState upTo) {
        // [2158] not implemented yet
    }

    @Override
    public String getManagedBeanName() {
        // [2158] not implemented yet
        return null;
    }

    @Override
    public Optional<? extends ObjectMember> getMember(Method method) {
        return Optional.empty();
    }

    @Override
    public Optional<ObjectAction> findObjectAction(String id, ActionType type) {
        // poorly implemented, inheritance not supported
        return getObjectAction(id, type);
    }
    
    @Override
    public Optional<ObjectAssociation> findAssociation(String id) {
        // poorly implemented, inheritance not supported
        return getAssociation(id);
    }

}
