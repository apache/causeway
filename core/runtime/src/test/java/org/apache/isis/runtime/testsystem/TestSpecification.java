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


package org.apache.isis.runtime.testsystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.metamodel.adapter.Instance;
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
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.metamodel.facets.FacetHolderNoop;
import org.apache.isis.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;


/**
 * @deprecated replaced by {@link TestProxySpecification}
 */
@Deprecated
public class TestSpecification extends FacetHolderNoop implements ObjectSpecification {

    private static int next = 100;
    private ObjectAction action;
    public ObjectAssociation[] fields = new ObjectAssociation[0];
    private final int id = next++;
    private final String name;
    private ObjectSpecification[] subclasses = new ObjectSpecification[0];
    private String title;
	private RuntimeContext runtimeContext;

    public TestSpecification() {
        this((String) null);
        this.runtimeContext = new RuntimeContextNoRuntime();
    }

    public TestSpecification(final Class<?> cls) {
        this(cls.getName());
    }

    public TestSpecification(final String name) {
        this.name = name == null ? "DummyObjectSpecification#" + id : name;
        title = "";
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

    public ObjectAdapter getAggregate(final ObjectAdapter object) {
        return null;
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

    public Object getFieldExtension(final String name, final Class<?> cls) {
        return null;
    }

    public Class<?>[] getFieldExtensions(final String name) {
        return new Class[0];
    }

    public ObjectAssociation[] getAssociations() {
        return fields;
    }

    public List<? extends ObjectAssociation> getAssociationList() {
        return Arrays.asList(getAssociations());
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

    public ObjectAction getObjectAction(final ObjectActionType type, final String name) {
        return getObjectAction(type, name, new ObjectSpecification[0]);
    }

    public ObjectAction[] getObjectActions(final ObjectActionType... type) {
        return null;
    }

    public List<? extends ObjectAction> getObjectActionList(final ObjectActionType... type) {
        return null;
    }

    public String getPluralName() {
        return null;
    }

    public Class<?> getSearchViaRepository() {
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
        return false;
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
        throw new IsisException("Not able to create instance of " + getFullName()
                + "; newInstance() method should be overridden");
    }

    public Persistability persistability() {
        return Persistability.USER_PERSISTABLE;
    }

    public boolean queryByExample() {
        return false;
    }

    public void setupAction(final ObjectAction action) {
        this.action = action;
    }

    public void setupFields(final ObjectAssociation[] fields) {
        this.fields = fields;
    }

    public void setupSubclasses(final ObjectSpecification[] subclasses) {
        this.subclasses = subclasses;
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

    @Override
    public Identifier getIdentifier() {
        return null;
    }

    public boolean isCollectionOrIsAggregated() {
        return false;
    }

    public Object createObject(CreationMode creationMode) {
        throw new NotYetImplementedException();
    }

    public boolean isHidden() {
        return false;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isNotCollection() {
        return !isCollection();
    }

    public boolean isImmutable() {
        return false;
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

    public Instance getInstance(ObjectAdapter adapter) {
        return adapter;
    }

	public RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}

}
