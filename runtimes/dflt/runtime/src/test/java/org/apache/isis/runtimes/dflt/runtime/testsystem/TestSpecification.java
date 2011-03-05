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


package org.apache.isis.runtimes.dflt.runtime.testsystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.adapters.Localization;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;


/**
 * @deprecated replaced by {@link TestProxySpecification}
 */
@Deprecated
public class TestSpecification extends FacetHolderNoop implements ObjectSpecification {

    private static int next = 100;
    private ObjectAction action;
    public List<ObjectAssociation> fields = Lists.newArrayList();
    private final int id = next++;
    private final String name;
    private List<ObjectSpecification> subclasses = Collections.emptyList();
    private String title;

    public TestSpecification() {
        this((String) null);
    }

    @Override
    public FeatureType getFeatureType() {
        return FeatureType.OBJECT;
    }
    

    public TestSpecification(final String name) {
        this.name = name == null ? "DummyObjectSpecification#" + id : name;
        title = "";
    }

    @Override
    public Class<?> getCorrespondingClass() {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void clearDirty(final ObjectAdapter object) {}

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public List<ObjectAction> getServiceActionsReturning(final ActionType... type) {
        return null;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isService() {
        return false;
    }

    @Override
    public ObjectAssociation getAssociation(final String name) {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getId().equals(name)) {
                return fields.get(i);
            }
        }
        throw new IsisException("Field not found: " + name);
    }

    @Override
    public List<ObjectAssociation> getAssociations() {
        return fields;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OneToOneAssociation> getProperties() {
        @SuppressWarnings("rawtypes")
        List list = getAssociations(ObjectAssociationFilters.PROPERTIES);
        return new ArrayList<OneToOneAssociation>(list);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OneToManyAssociation> getCollections() {
        @SuppressWarnings("rawtypes")
        List list = getAssociations(ObjectAssociationFilters.COLLECTIONS);
        return new ArrayList<OneToManyAssociation>(list);
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
    public String getFullIdentifier() {
        return name;
    }

    @Override
    public String getIconName(final ObjectAdapter reference) {
        return null;
    }

    @Override
    public ObjectAction getObjectAction(
            final ActionType type,
            final String name,
            final List<ObjectSpecification> parameters) {
        if (action != null && action.getId().equals(name)) {
            return action;
        }
        return null;
    }

    @Override
    public ObjectAction getObjectAction(final ActionType type, final String name) {
        return getObjectAction(type, name, ObjectSpecification.EMPTY_LIST);
    }

    @Override
    public List<ObjectAction> getObjectActions(final ActionType... type) {
        return null;
    }

    @Override
    public String getPluralName() {
        return null;
    }

    public Class<?> getSearchViaRepository() {
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
    
    public String getHelp() {
        return null;
    }

    @Override
    public String getTitle(final ObjectAdapter adapter, Localization localization) {
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
    public boolean isDirty(final ObjectAdapter object) {
        return false;
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
    public boolean isValueOrIsAggregated() {
        return false;
    }

    @Override
    public boolean isValue() {
        return false;
    }

    @Override
    public boolean isAggregated() {
        return false;
    }

    @Override
    public void markDirty(final ObjectAdapter object) {}

    public Object newInstance() {
        throw new IsisException("Not able to create instance of " + getFullIdentifier()
                + "; newInstance() method should be overridden");
    }

    @Override
    public Persistability persistability() {
        return Persistability.USER_PERSISTABLE;
    }

    public void setupAction(final ObjectAction action) {
        this.action = action;
    }

    @Override
    public List<ObjectSpecification> subclasses() {
        return subclasses;
    }

    @Override
    public ObjectSpecification superclass() {
        return null;
    }

    @Override
    public String toString() {
        return getFullIdentifier();
    }

    @Override
    public Consent isValid(final ObjectAdapter transientObject) {
        return null;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public Identifier getIdentifier() {
        return null;
    }

    @Override
    public boolean isCollectionOrIsAggregated() {
        return false;
    }

    @Override
    public Object createObject(CreationMode creationMode) {
        throw new NotYetImplementedException();
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isCollection() {
        return false;
    }
    
    @Override
    public boolean isNotCollection() {
        return !isCollection();
    }

    @Override
    public boolean isImmutable() {
        return false;
    }


    @Override
    public ObjectValidityContext createValidityInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObjectAdapter) {
        return null;
    }

    @Override
    public ObjectTitleContext createTitleInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObjectAdapter) {
        return null;
    }

    @Override
    public InteractionResult isValidResult(final ObjectAdapter transientObject) {
        return null;
    }


    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////

    @Override
    public Instance getInstance(ObjectAdapter adapter) {
        return adapter;
    }


    // /////////////////////////////////////////////////////////////
    // introspection
    // /////////////////////////////////////////////////////////////

    @Override
    public void introspectTypeHierarchyAndMembers() {
    }

    @Override
    public void updateFromFacetValues() {
    }

    @Override
    public void markAsService() {
    }

    @Override
    public boolean isIntrospected() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer#getObjectActionsAll()
     */
    @Override
    public List<ObjectAction> getObjectActionsAll() {
        // TODO Auto-generated method stub
        return null;
    }



}
