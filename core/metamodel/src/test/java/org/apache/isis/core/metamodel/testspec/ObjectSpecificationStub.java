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
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class ObjectSpecificationStub extends FacetHolderImpl implements ObjectSpecification {

    private ObjectAction action;
    public List<ObjectAssociation> fields = Lists.newArrayList();
    private final String name;
    private List<ObjectSpecification> subclasses = Collections.emptyList();
    private String title;
    /**
     * lazily derived, see {@link #getSpecId()} 
     */
    private ObjectSpecId specId;

    private Persistability persistable;
    private boolean isEncodeable;

    private RuntimeContextNoRuntime runtimeContext;

    public ObjectSpecificationStub(final Class<?> type) {
        this(type.getName());
        runtimeContext = new RuntimeContextNoRuntime();
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
        persistable = Persistability.USER_PERSISTABLE;
    }

    @Override
    public FeatureType getFeatureType() {
        return FeatureType.OBJECT;
    }

    @Override
    public void clearDirty(final ObjectAdapter object) {
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void debugData(final DebugBuilder debug) {
    }

    public String debugInterface() {
        return null;
    }

    public String debugTitle() {
        return "";
    }

    public ObjectAction getClassAction(final ActionType type, final String name, final ObjectSpecification[] parameters) {
        return null;
    }

    @Override
    public List<ObjectAction> getServiceActionsReturning(final List<ActionType> types) {
        return null;
    }

    public int getFeatures() {
        return 0;
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
    public List<ObjectAssociation> getAssociations(final Contributed contributed) {
        return fields;
    }

    @Deprecated
    @Override
    public List<ObjectAssociation> getAssociations(final Filter<ObjectAssociation> filter) {
        return getAssociations(Contributed.INCLUDED, filter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OneToOneAssociation> getProperties(final Contributed contributed) {
        @SuppressWarnings("rawtypes")
        final List list = getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.PROPERTIES);
        return new ArrayList<OneToOneAssociation>(list);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OneToManyAssociation> getCollections(final Contributed contributed) {
        @SuppressWarnings("rawtypes")
        final List list = getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.COLLECTIONS);
        return new ArrayList<>(list);
    }

    @Override
    public List<ObjectAssociation> getAssociations(final Contributed contributed, final Filter<ObjectAssociation> filter) {
        final List<ObjectAssociation> allFields = getAssociations(Contributed.EXCLUDED);

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
    public ObjectSpecId getSpecId() {
        if(specId == null) {
            specId = getFacet(ObjectSpecIdFacet.class).value();
        }
        return specId;
    }

    @Override
    public String getIconName(final ObjectAdapter reference) {
        return null;
    }

    @Override
    public String getCssClass() {
        return null;
    }

    @Override
    public String getCssClass(final ObjectAdapter reference) {
        return null;
    }

    @Override
    public ObjectAction getObjectAction(final ActionType type, final String name, final List<ObjectSpecification> parameters) {
        if (action != null && action.getId().equals(name)) {
            return action;
        }
        return null;
    }

    @Override
    public ObjectAction getObjectAction(final ActionType type, final String id) {
        final int openBracket = id.indexOf('(');
        return getObjectAction(type, id.substring(0, openBracket), null);
    }

    @Override
    public ObjectAction getObjectAction(final String nameParmsIdentityString) {
        for (final ActionType type : ActionType.values()) {
            final ObjectAction action = getObjectAction(type, nameParmsIdentityString);
            if (action != null) {
                return action;
            }
        }
        return null;
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
    public String getTitle(final ObjectAdapter targetAdapter, final Localization localization) {
        return getTitle(null, targetAdapter, localization);
    }

    @Override
    public String getTitle(final ObjectAdapter contextAdapterIfAny, final ObjectAdapter targetAdapter, final Localization localization) {
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

    public void introspect() {
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
        return isEncodeable;
    }

    @Override
    public boolean isParseable() {
        return false;
    }

    @Override
    public boolean isValueOrIsParented() {
        return false;
    }

    @Override
    public boolean isValue() {
        return false;
    }

    @Override
    public boolean isParented() {
        return false;
    }

    @Override
    public void markDirty(final ObjectAdapter object) {
    }

    public Object newInstance() {
        return InstanceUtil.createInstance(name);
    }

    @Override
    public Persistability persistability() {
        return persistable;
    }

    public void setupAction(final ObjectAction action) {
        this.action = action;
    }

    public void setupFields(final List<ObjectAssociation> fields) {
        this.fields = fields;
    }

    public void setupIsEncodeable() {
        isEncodeable = true;
    }

    public void setupSubclasses(final List<ObjectSpecification> subclasses) {
        this.subclasses = subclasses;
    }

    public void setupHasNoIdentity(final boolean hasNoIdentity) {
    }

    public void setupTitle(final String title) {
        this.title = title;
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
    public Consent isValid(final ObjectAdapter transientObject) {
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
    public Object createObject() {
        try {
            final Class<?> cls = Class.forName(name);
            return cls.newInstance();
        } catch (final ClassNotFoundException e) {
            throw new IsisException(e);
        } catch (final InstantiationException e) {
            throw new IsisException(e);
        } catch (final IllegalAccessException e) {
            throw new IsisException(e);
        }
    }

    @Override
    public ObjectAdapter initialize(final ObjectAdapter objectAdapter) {
        return objectAdapter;
    }

    public void setupPersistable(final Persistability persistable) {
        this.persistable = persistable;
    }

    @Override
    public boolean isParentedOrFreeCollection() {
        return false;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isNotCollection() {
        return !isParentedOrFreeCollection();
    }

    @Override
    public boolean isImmutable() {
        return containsFacet(ImmutableFacet.class);
    }

    @Override
    public ObjectValidityContext createValidityInteractionContext(final DeploymentCategory deploymentCategory, final AuthenticationSession session, final InteractionInvocationMethod invocationMethod, final ObjectAdapter targetObjectAdapter) {
        return null;
    }

    @Override
    public ObjectTitleContext createTitleInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod invocationMethod, final ObjectAdapter targetObjectAdapter) {
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
    public ObjectAdapter getInstance(final ObjectAdapter adapter) {
        return adapter;
    }

    public RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    // /////////////////////////////////////////////////////////////
    // introspection
    // /////////////////////////////////////////////////////////////

    @Override
    public void markAsService() {
    }

    @Override
    public List<ObjectAction> getObjectActions(final Contributed contributed) {
        return null;
    }

    @Override
    public List<ObjectAction> getObjectActions(final ActionType type, final Contributed contributed, final Filter<ObjectAction> filter) {
        return null;
    }

    @Override
    public List<ObjectAction> getObjectActions(final List<ActionType> types, final Contributed contributed, final Filter<ObjectAction> filter) {
        return null;
    }



    // /////////////////////////////////////////////////////////
    // view models and wizards
    // /////////////////////////////////////////////////////////

    @Override
    public boolean isViewModel() {
        return false;
    }

    @Override
    public boolean isViewModelCloneable(final ObjectAdapter targetAdapter) {
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


}
