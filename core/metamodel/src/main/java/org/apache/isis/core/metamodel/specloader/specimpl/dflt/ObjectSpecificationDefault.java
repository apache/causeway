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

package org.apache.isis.core.metamodel.specloader.specimpl.dflt;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.all.i18n.NamedFacetTranslated;
import org.apache.isis.core.metamodel.facets.all.i18n.PluralFacetTranslated;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetInferred;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.facets.object.plural.inferred.PluralFacetInferred;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.object.wizard.WizardFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.specimpl.FacetedMethodsBuilder;
import org.apache.isis.core.metamodel.specloader.specimpl.FacetedMethodsBuilderContext;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionDefault;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToManyAssociationDefault;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToOneAssociationDefault;

public class ObjectSpecificationDefault extends ObjectSpecificationAbstract implements FacetHolder {

    private final static Logger LOG = LoggerFactory.getLogger(ObjectSpecificationDefault.class);

    private static final ClassSubstitutor classSubstitutor = new ClassSubstitutor();

    private static String determineShortName(final Class<?> introspectedClass) {
        final String name = introspectedClass.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    //region > constructor, fields

    /**
     * Lazily built by {@link #getMember(Method)}.
     */
    private Map<Method, ObjectMember> membersByMethod = null;
    
    private final FacetedMethodsBuilder facetedMethodsBuilder;
    private final boolean isService;


    public ObjectSpecificationDefault(
            final Class<?> correspondingClass,
            final FacetedMethodsBuilderContext facetedMethodsBuilderContext,
            final ServicesInjector servicesInjector,
            final FacetProcessor facetProcessor,
            final NatureOfService natureOfServiceIfAny) {
        super(correspondingClass, determineShortName(correspondingClass),
                servicesInjector, facetProcessor);

        this.isService = natureOfServiceIfAny != null;
        this.facetedMethodsBuilder = new FacetedMethodsBuilder(this, facetedMethodsBuilderContext);
    }


    //endregion

    //region > introspectTypeHierarchyAndMembers
    @Override
    public void introspectTypeHierarchyAndMembers() {

        metadataProperties = null;
        if(isNotIntrospected()) {
            metadataProperties = facetedMethodsBuilder.introspectClass();
        }
        
        // name
        if(isNotIntrospected()) {
            addNamedFacetAndPluralFacetIfRequired();
        }

        // go no further if a value
        if(this.containsFacet(ValueFacet.class)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("skipping full introspection for value type {}", getFullIdentifier());
            }
            return;
        }

        // superclass
        if(isNotIntrospected()) {
            final Class<?> superclass = getCorrespondingClass().getSuperclass();
            updateSuperclass(superclass);
        }


        // walk superinterfaces

        //
        // REVIEW: the processing here isn't quite the same as with
        // superclasses, in that with superclasses the superclass adds this type as its
        // subclass, whereas here this type defines itself as the subtype.
        //
        // it'd be nice to push the responsibility for adding subclasses to
        // the interface type... needs some tests around it, though, before
        // making that refactoring.
        //
        final Class<?>[] interfaceTypes = getCorrespondingClass().getInterfaces();
        final List<ObjectSpecification> interfaceSpecList = Lists.newArrayList();
        for (final Class<?> interfaceType : interfaceTypes) {
            final Class<?> substitutedInterfaceType = classSubstitutor.getClass(interfaceType);
            if (substitutedInterfaceType != null) {
                final ObjectSpecification interfaceSpec = getSpecificationLoader().loadSpecification(substitutedInterfaceType);
                interfaceSpecList.add(interfaceSpec);
            }
        }

        if(isNotIntrospected()) {
            updateAsSubclassTo(interfaceSpecList);
        }
        if(isNotIntrospected()) {
            updateInterfaces(interfaceSpecList);
        }

        updateAssociationsAndActions();
    }

    private synchronized void updateAssociationsAndActions() {

        // associations and actions
        if(isNotIntrospected()) {
            final List<ObjectAssociation> associations = createAssociations(metadataProperties);
            sortAndUpdateAssociations(associations);
        }

        if(isNotIntrospected()) {
            final List<ObjectAction> actions = createActions(metadataProperties);
            sortCacheAndUpdateActions(actions);
        }

        if(isNotIntrospected()) {
            updateFromFacetValues();
        }
    }

    private void addNamedFacetAndPluralFacetIfRequired() {
        NamedFacet namedFacet = getFacet(NamedFacet.class);
        if (namedFacet == null) {
            namedFacet = new NamedFacetInferred(StringExtensions.asNaturalName2(getShortIdentifier()), this);
            addFacet(namedFacet);
        }

        PluralFacet pluralFacet = getFacet(PluralFacet.class);
        if (pluralFacet == null) {
            if(namedFacet instanceof NamedFacetTranslated) {
                final NamedFacetTranslated facet = (NamedFacetTranslated) namedFacet;
                pluralFacet = new PluralFacetTranslated(facet, this);
            } else {
                pluralFacet = new PluralFacetInferred(StringExtensions.asPluralName(namedFacet.value()), this);
            }
            addFacet(pluralFacet);
        }
    }

    //endregion

    //region > create associations and actions
    private List<ObjectAssociation> createAssociations(Properties properties) {
        final List<FacetedMethod> associationFacetedMethods = facetedMethodsBuilder.getAssociationFacetedMethods(properties);
        final List<ObjectAssociation> associations = Lists.newArrayList();
        for (FacetedMethod facetedMethod : associationFacetedMethods) {
            final ObjectAssociation association = createAssociation(facetedMethod);
            if(association != null) {
                associations.add(association);
            }
        }
        return associations;
    }
    

    private ObjectAssociation createAssociation(final FacetedMethod facetMethod) {
        if (facetMethod.getFeatureType().isCollection()) {
            return new OneToManyAssociationDefault(facetMethod, servicesInjector);
        } else if (facetMethod.getFeatureType().isProperty()) {
            return new OneToOneAssociationDefault(facetMethod, servicesInjector);
        } else {
            return null;
        }
    }

    private List<ObjectAction> createActions(Properties metadataProperties) {
        final List<FacetedMethod> actionFacetedMethods = facetedMethodsBuilder.getActionFacetedMethods(metadataProperties);
        final List<ObjectAction> actions = Lists.newArrayList();
        for (FacetedMethod facetedMethod : actionFacetedMethods) {
            final ObjectAction action = createAction(facetedMethod);
            if(action != null) {
                actions.add(action);
            }
        }
        return actions;
    }


    private ObjectAction createAction(final FacetedMethod facetedMethod) {
        if (facetedMethod.getFeatureType().isAction()) {
            return new ObjectActionDefault(facetedMethod, servicesInjector);
        } else {
            return null;
        }
    }

    //endregion

    //region > isXxx

    @Override
    public boolean isViewModel() {
        return containsFacet(ViewModelFacet.class);
    }

    @Override
    public boolean isViewModelCloneable(ObjectAdapter targetAdapter) {
        final ViewModelFacet facet = getFacet(ViewModelFacet.class);
        if(facet == null) {
            return false;
        }
        final Object pojo = targetAdapter.getObject();
        return facet.isCloneable(pojo);
    }

    @Override
    public boolean isMixin() {
        return containsFacet(MixinFacet.class);
    }

    @Override
    public boolean isWizard() {
        return containsFacet(WizardFacet.class);
    }

    @Override
    public boolean isService() {
        return isService;
    }

    //endregion

    //region > getObjectAction

    @Override
    public ObjectAction getObjectAction(final ActionType type, final String id, final List<ObjectSpecification> parameters) {
        final List<ObjectAction> actions = 
                getObjectActions(type, Contributed.INCLUDED, Filters.<ObjectAction>any());
        return firstAction(actions, id, parameters);
    }

    @Override
    public ObjectAction getObjectAction(final ActionType type, final String id) {
        final List<ObjectAction> actions = 
                getObjectActions(type, Contributed.INCLUDED, Filters.<ObjectAction>any()); 
        return firstAction(actions, id);
    }

    @Override
    public ObjectAction getObjectAction(final String id) {
        final List<ObjectAction> actions = 
                getObjectActions(ActionType.ALL, Contributed.INCLUDED, Filters.<ObjectAction>any()); 
        return firstAction(actions, id);
    }

    private static ObjectAction firstAction(
            final List<ObjectAction> candidateActions, 
            final String actionName, 
            final List<ObjectSpecification> parameters) {
        outer: for (int i = 0; i < candidateActions.size(); i++) {
            final ObjectAction action = candidateActions.get(i);
            if (actionName != null && !actionName.equals(action.getId())) {
                continue outer;
            }
            if (action.getParameters().size() != parameters.size()) {
                continue outer;
            }
            for (int j = 0; j < parameters.size(); j++) {
                if (!parameters.get(j).isOfType(action.getParameters().get(j).getSpecification())) {
                    continue outer;
                }
            }
            return action;
        }
        return null;
    }

    private static ObjectAction firstAction(
            final List<ObjectAction> candidateActions, 
            final String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < candidateActions.size(); i++) {
            final ObjectAction action = candidateActions.get(i);
            if (id.equals(action.getIdentifier().toNameParmsIdentityString())) {
                return action;
            }
            if (id.equals(action.getIdentifier().toNameIdentityString())) {
                return action;
            }
            continue;
        }
        return null;
    }

    //endregion

    //region > getMember, catalog... (not API)

    public ObjectMember getMember(final Method method) {
        if (membersByMethod == null) {
            this.membersByMethod = catalogueMembers();
        }
        return membersByMethod.get(method);
    }

    private HashMap<Method, ObjectMember> catalogueMembers() {
        final HashMap<Method, ObjectMember> membersByMethod = Maps.newHashMap();
        cataloguePropertiesAndCollections(membersByMethod);
        catalogueActions(membersByMethod);
        return membersByMethod;
    }
    
    private void cataloguePropertiesAndCollections(final Map<Method, ObjectMember> membersByMethod) {
        final Filter<ObjectAssociation> noop = Filters.anyOfType(ObjectAssociation.class);
        final List<ObjectAssociation> fields = getAssociations(Contributed.EXCLUDED, noop);
        for (int i = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
            final List<Facet> facets = field.getFacets(ImperativeFacet.FILTER);
            for (final Facet facet : facets) {
                final ImperativeFacet imperativeFacet = ImperativeFacet.Util.getImperativeFacet(facet);
                for (final Method imperativeFacetMethod : imperativeFacet.getMethods()) {
                    membersByMethod.put(imperativeFacetMethod, field);
                }
            }
        }
    }

    private void catalogueActions(final Map<Method, ObjectMember> membersByMethod) {
        final List<ObjectAction> userActions = getObjectActions(Contributed.INCLUDED);
        for (int i = 0; i < userActions.size(); i++) {
            final ObjectAction userAction = userActions.get(i);
            final List<Facet> facets = userAction.getFacets(ImperativeFacet.FILTER);
            for (final Facet facet : facets) {
                final ImperativeFacet imperativeFacet = ImperativeFacet.Util.getImperativeFacet(facet);
                for (final Method imperativeFacetMethod : imperativeFacet.getMethods()) {
                    membersByMethod.put(imperativeFacetMethod, userAction);
                }
            }
        }
    }

    //endregion

    //region > toString
    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("class", getFullIdentifier());
        str.append("type", (isParentedOrFreeCollection() ? "Collection" : "Object"));
        str.append("persistable", persistability());
        str.append("superclass", superclass() == null ? "Object" : superclass().getFullIdentifier());
        return str.toString();
    }

    //endregion

}
