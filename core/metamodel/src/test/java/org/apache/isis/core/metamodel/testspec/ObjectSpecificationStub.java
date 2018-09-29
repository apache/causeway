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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

public class ObjectSpecificationStub extends FacetHolderImpl implements ObjectSpecification {

    private ObjectAction action;
    public List<ObjectAssociation> fields = _Lists.newArrayList();
    private final String name;
    private List<ObjectSpecification> subclasses = Collections.emptyList();
    private String title;
    /**
     * lazily derived, see {@link #getSpecId()} 
     */
    private ObjectSpecId specId;

    private ServicesInjector servicesInjector;
    private ObjectSpecification elementSpecification;

    public ObjectSpecificationStub(final Class<?> type) {
        this(type.getName());
        IsisConfigurationDefault stubConfiguration = new IsisConfigurationDefault(null);
        this.servicesInjector = new ServicesInjector(Collections.emptyList(), stubConfiguration);
        servicesInjector.addFallbackIfRequired(ConfigurationServiceInternal.class, stubConfiguration);
    }

    @Override
    public ObjectMember getMember(final String memberId) {
        final ObjectAction objectAction = getObjectAction(memberId);
        if(objectAction != null) {
            return objectAction;
        }
        final ObjectAssociation association = getAssociation(memberId);
        if(association != null) {
            return association;
        }
        return null;
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
    public Stream<ObjectAssociation> streamAssociations(final Contributed contributed) {
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
    public List<ObjectSpecification> subclasses() {
        return subclasses;
    }

    @Override
    public List<ObjectSpecification> subclasses(final Depth depth) {
        return subclasses();
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
    public ObjectValidityContext createValidityInteractionContext(
            ManagedObject targetAdapter, InteractionInitiatedBy interactionInitiatedBy) {
        return null;
    }

    @Override
    public ObjectTitleContext createTitleInteractionContext(
            AuthenticationSession session, InteractionInitiatedBy invocationMethod, ManagedObject targetObjectAdapter) {
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
    public Stream<ObjectAction> streamObjectActions(final Contributed contributed) {
        return null;
    }

    @Override
    public Stream<ObjectAction> streamObjectActions(final ActionType type, final Contributed contributed) {
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
    public boolean isViewModelCloneable(final ManagedObject targetAdapter) {
        return false;
    }

    @Override
    public boolean isWizard() {
        return false;
    }

    @Override
    public boolean isMixin() {
        return false;
    }

    @Override
    public boolean isPersistenceCapable() {
        return false;
    }

    @Override
    public boolean isPersistenceCapableOrViewModel() {
        return false;
    }

    @Override
    public String toString() {
        return getFullIdentifier();
    }

    @Override
    public ObjectSpecification getElementSpecification() {
        return elementSpecification;
    }

}
