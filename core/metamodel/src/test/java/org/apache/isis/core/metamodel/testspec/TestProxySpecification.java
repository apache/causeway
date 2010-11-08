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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceFactory;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.metamodel.facets.FacetHolderImpl;
import org.apache.isis.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;


public class TestProxySpecification extends FacetHolderImpl implements ObjectSpecification {

    private ObjectAction action;
    public ObjectAssociation[] fields = new ObjectAssociation[0];
    private final String name;
    private ObjectSpecification[] subclasses = new ObjectSpecification[0];
    private String title;

    private Persistability persistable;
    private boolean isEncodeable;
    private boolean hasNoIdentity;
	private RuntimeContextNoRuntime runtimeContext;

    public TestProxySpecification(final Class<?> type) {
        this(type.getName());
		runtimeContext = new RuntimeContextNoRuntime();
    }

    public TestProxySpecification(final String name) {
        this.name = name;
        title = "";
        persistable = Persistability.USER_PERSISTABLE;
    }

    public void addSubclass(final ObjectSpecification specification) {}

    public void clearDirty(final ObjectAdapter object) {}

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void debugData(final DebugString debug) {}

    public String debugInterface() {
        return null;
    }

    public String debugTitle() {
        return "";
    }

    public ObjectAction getClassAction(
            final ObjectActionType type,
            final String name,
            final ObjectSpecification[] parameters) {
        return null;
    }

    public ObjectAction[] getServiceActionsFor(final ObjectActionType... type) {
        return null;
    }

    public int getFeatures() {
        return 0;
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isService() {
        return false;
    }

    public ObjectAssociation getAssociation(final String name) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getId().equals(name)) {
                return fields[i];
            }
        }
        throw new IsisException("Field not found: " + name);
    }

    public ObjectAssociation[] getAssociations() {
        return fields;
    }

    public List<? extends ObjectAssociation> getAssociationList() {
        return Arrays.asList(fields);
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

    public String getFullName() {
        return name;
    }

    public String getIconName(final ObjectAdapter reference) {
        return null;
    }

    public ObjectAction getObjectAction(
            final ObjectActionType type,
            final String name,
            final ObjectSpecification[] parameters) {
        if (action != null && action.getId().equals(name)) {
            return action;
        }
        return null;
    }

    public ObjectAction getObjectAction(final ObjectActionType type, final String id) {
    	int openBracket = id.indexOf('(');
        return getObjectAction(type, id.substring(0, openBracket), null);
    }

    public ObjectAction[] getObjectActions(final ObjectActionType... types) {
        return null;
    }

    public List<? extends ObjectAction> getObjectActionList(final ObjectActionType... types) {
        return null;
    }

    public String getPluralName() {
        return null;
    }

    public String getShortName() {
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public String getSingularName() {
        return name + " (singular)";
    }

    public String getDescription() {
        return getSingularName();
    }

    public String getTitle(final ObjectAdapter adapter) {
        return title;
    }

    public boolean hasSubclasses() {
        return false;
    }

    public ObjectSpecification[] interfaces() {
        return new ObjectSpecification[0];
    }

    public void introspect() {}

    public boolean isDirty(final ObjectAdapter object) {
        return false;
    }

    public boolean isOfType(final ObjectSpecification cls) {
        return cls == this;
    }

    public boolean isEncodeable() {
        return isEncodeable;
    }

    public boolean isParseable() {
        return false;
    }

    public boolean isValueOrIsAggregated() {
        return false;
    }

    public boolean isValue() {
        return false;
    }

    public boolean isAggregated() {
        return false;
    }

    public void markDirty(final ObjectAdapter object) {}

    public Object newInstance() {
        return InstanceFactory.createInstance(name);
    }

    public Persistability persistability() {
        return persistable;
    }

    public void setupAction(final ObjectAction action) {
        this.action = action;
    }

    public void setupFields(final ObjectAssociation[] fields) {
        this.fields = fields;
    }

    public void setupIsEncodeable() {
        isEncodeable = true;
    }

    public void setupSubclasses(final ObjectSpecification[] subclasses) {
        this.subclasses = subclasses;
    }

    public void setupHasNoIdentity(final boolean hasNoIdentity) {
        this.hasNoIdentity = hasNoIdentity;
    }

    public void setupTitle(final String title) {
        this.title = title;
    }

    public ObjectSpecification[] subclasses() {
        return subclasses;
    }

    public ObjectSpecification superclass() {
        return null;
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public Consent isValid(final ObjectAdapter transientObject) {
        return null;
    }

    public Object getDefaultValue() {
        return null;
    }

    public Identifier getIdentifier() {
        return Identifier.classIdentifier(name);
    }

    public boolean isCollectionOrIsAggregated() {
        return hasNoIdentity;
    }

    public Object createObject(CreationMode creationMode) {
        try {
            final Class<?> cls = Class.forName(name);
            return cls.newInstance();
        } catch (ClassNotFoundException e) {
            throw new IsisException(e);
        } catch (InstantiationException e) {
            throw new IsisException(e);
        } catch (IllegalAccessException e) {
            throw new IsisException(e);
        }
    }

    public void setupPersistable(final Persistability persistable) {
        this.persistable = persistable;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isHidden() {
        return false;
    }

    public boolean isNotCollection() {
        return !isCollection();
    }

    public boolean isImmutable() {
        return containsFacet(ImmutableFacet.class);
    }

    public ObjectValidityContext createValidityInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObjectAdapter) {
        return null;
    }

    public ObjectTitleContext createTitleInteractionContext(
            final AuthenticationSession session,
            final InteractionInvocationMethod invocationMethod,
            final ObjectAdapter targetObjectAdapter) {
        return null;
    }

    public InteractionResult isValidResult(final ObjectAdapter transientObject) {
        return null;
    }

    // /////////////////////////////////////////////////////////////
    // getInstance
    // /////////////////////////////////////////////////////////////

    public ObjectAdapter getInstance(ObjectAdapter adapter) {
        return adapter;
    }

	public RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}


}

