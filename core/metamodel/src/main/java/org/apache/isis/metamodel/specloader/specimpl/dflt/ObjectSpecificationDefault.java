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

package org.apache.isis.metamodel.specloader.specimpl.dflt;

import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.metamodel.commons.StringExtensions;
import org.apache.isis.metamodel.commons.ToString;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.ImperativeFacet;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.all.i18n.NamedFacetTranslated;
import org.apache.isis.metamodel.facets.all.i18n.PluralFacetTranslated;
import org.apache.isis.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.metamodel.facets.all.named.NamedFacetInferred;
import org.apache.isis.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.metamodel.facets.object.plural.inferred.PluralFacetInferred;
import org.apache.isis.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.facets.object.wizard.WizardFacet;
import org.apache.isis.metamodel.spec.ActionType;
import org.apache.isis.metamodel.spec.ElementSpecificationProvider;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.isis.metamodel.specloader.specimpl.FacetedMethodsBuilder;
import org.apache.isis.metamodel.specloader.specimpl.FacetedMethodsBuilderContext;
import org.apache.isis.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.metamodel.specloader.specimpl.ObjectActionDefault;
import org.apache.isis.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.metamodel.specloader.specimpl.OneToManyAssociationDefault;
import org.apache.isis.metamodel.specloader.specimpl.OneToOneAssociationDefault;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ObjectSpecificationDefault extends ObjectSpecificationAbstract implements FacetHolder {

    private static final ClassSubstitutor classSubstitutor = new ClassSubstitutor();

    private static String determineShortName(final Class<?> introspectedClass) {
        final String name = introspectedClass.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    // -- constructor, fields

    /**
     * Lazily built by {@link #getMember(Method)}.
     */
    private Map<Method, ObjectMember> membersByMethod = null;

    private final FacetedMethodsBuilder facetedMethodsBuilder;
    private final boolean isManagedBean;

    public ObjectSpecificationDefault(
            final Class<?> correspondingClass,
            final FacetedMethodsBuilderContext facetedMethodsBuilderContext,
            final FacetProcessor facetProcessor,
            final boolean isManagedBean,
            final PostProcessor postProcessor) {
        super(correspondingClass, determineShortName(correspondingClass), facetProcessor, postProcessor);

        this.isManagedBean = isManagedBean;
        
        this.facetedMethodsBuilder = new FacetedMethodsBuilder(this, facetedMethodsBuilderContext);

        facetProcessor.processObjectSpecId(correspondingClass, this);
    }

    @Override
    protected void introspectTypeHierarchy() {

            facetedMethodsBuilder.introspectClass();

        // name
            addNamedFacetAndPluralFacetIfRequired();

        // go no further if a value
        if(this.containsFacet(ValueFacet.class)) {
            if (log.isDebugEnabled()) {
                log.debug("skipping type hierarchy introspection for value type {}", getFullIdentifier());
            }
            return;
        }

        final DomainServiceFacet facet = getFacet(DomainServiceFacet.class);
        final boolean serviceWithNatureOfDomain = facet != null && facet.getNatureOfService() == NatureOfService.DOMAIN;
        if (serviceWithNatureOfDomain) {
            if (log.isDebugEnabled()) {
                log.debug("skipping type hierarchy introspection for domain service with natureOfService = DOMAIN {}", getFullIdentifier());
            }
            return;
        }

        // superclass
            final Class<?> superclass = getCorrespondingClass().getSuperclass();
        loadSpecOfSuperclass(superclass);

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
        final List<ObjectSpecification> interfaceSpecList = _Lists.newArrayList();
        for (final Class<?> interfaceType : interfaceTypes) {
            final Class<?> substitutedInterfaceType = classSubstitutor.getClass(interfaceType);
            if (substitutedInterfaceType != null) {
                final ObjectSpecification interfaceSpec = getSpecificationLoader().loadSpecification(substitutedInterfaceType);
                interfaceSpecList.add(interfaceSpec);
            }
        }

            updateAsSubclassTo(interfaceSpecList);
            updateInterfaces(interfaceSpecList);
        }

    protected synchronized void introspectMembers() {

        if(this.containsFacet(ValueFacet.class)) {
            if (log.isDebugEnabled()) {
                log.debug("skipping full introspection for value type {}", getFullIdentifier());
            }
            return;
    }

        // associations and actions
            final List<ObjectAssociation> associations = createAssociations();
            sortAndUpdateAssociations(associations);

            final List<ObjectAction> actions = createActions();
            sortCacheAndUpdateActions(actions);

        postProcess();
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


    // -- create associations and actions
    private List<ObjectAssociation> createAssociations() {
        final List<ObjectAssociation> associations = _Lists.newArrayList();
        if(isExcludedFromMetamodel()) {
            // add no associations
        } else {
            final List<FacetedMethod> associationFacetedMethods = facetedMethodsBuilder.getAssociationFacetedMethods();
            for (FacetedMethod facetedMethod : associationFacetedMethods) {
                final ObjectAssociation association = createAssociation(facetedMethod);
                if(association != null) {
                    associations.add(association);
                }
            }
        }
        return associations;
    }

    private ObjectAssociation createAssociation(final FacetedMethod facetMethod) {
        if (facetMethod.getFeatureType().isCollection()) {
            return new OneToManyAssociationDefault(facetMethod);
        } else if (facetMethod.getFeatureType().isProperty()) {
            return new OneToOneAssociationDefault(facetMethod);
        } else {
            return null;
        }
    }

    private List<ObjectAction> createActions() {
        final List<ObjectAction> actions = _Lists.newArrayList();
        if(isExcludedFromMetamodel()) {
            // create no actions
        } else {
            final List<FacetedMethod> actionFacetedMethods = facetedMethodsBuilder.getActionFacetedMethods();
            for (FacetedMethod facetedMethod : actionFacetedMethods) {
                final ObjectAction action = createAction(facetedMethod);
                if(action != null) {
                    actions.add(action);
                }
            }
        }
        return actions;
    }


    private ObjectAction createAction(final FacetedMethod facetedMethod) {
        if (facetedMethod.getFeatureType().isAction()) {
            return new ObjectActionDefault(facetedMethod);
        } else {
            return null;
        }
    }

    // -- PREDICATES

    @Override
    public boolean isViewModelCloneable(ManagedObject targetAdapter) {
        final ViewModelFacet facet = getFacet(ViewModelFacet.class);
        if(facet == null) {
            return false;
        }
        final Object pojo = targetAdapter.getPojo();
        return facet.isCloneable(pojo);
    }

    @Override
    public boolean isWizard() {
        return containsFacet(WizardFacet.class);
    }

    @Override
    public boolean isManagedBean() {
        return isManagedBean;
    }

    // -- getObjectAction

    @Override
    public ObjectAction getObjectAction(final ActionType type, final String id, final List<ObjectSpecification> parameters) {
        introspectUpTo(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        final Stream<ObjectAction> actions =
                streamObjectActions(type, Contributed.INCLUDED);
        return firstAction(actions, id, parameters);
    }

    @Override
    public ObjectAction getObjectAction(final ActionType type, final String id) {
        introspectUpTo(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        final Stream<ObjectAction> actions =
                streamObjectActions(type, Contributed.INCLUDED);
        return firstAction(actions, id);
    }

    @Override
    public ObjectAction getObjectAction(final String id) {
        introspectUpTo(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        final Stream<ObjectAction> actions =
                streamObjectActions(ActionType.ALL, Contributed.INCLUDED);
        return firstAction(actions, id);
    }

    private static ObjectAction firstAction(
            final Stream<ObjectAction> candidateActions,
            final String actionName,
            final List<ObjectSpecification> parameters) {
        
        return candidateActions
            .filter(action->actionName == null || actionName.equals(action.getId()))
            .filter(action->isMatchingSignature(parameters, action.getParameters()))
            .findAny()
            .orElse(null);
    }
    
    private static  boolean isMatchingSignature(
            final List<ObjectSpecification> a,
            final List<ObjectActionParameter> b) {
        
        if(a.size() != b.size()) {
            return false;
        }
        for (int j = 0; j < a.size(); j++) {
            if (!a.get(j).isOfType(b.get(j).getSpecification())) {
                return false;
            }
        }
        return true;
    }

    private static ObjectAction firstAction(
            final Stream<ObjectAction> candidateActions,
            final String id) {
        
        if (id == null) {
            return null;
        }
        
        return candidateActions
                .filter(action->{
                    final Identifier identifier = action.getIdentifier();

                    if (id.equals(identifier.toNameParmsIdentityString())) {
                        return true;
                    }
                    if (id.equals(identifier.toNameIdentityString())) {
                        return true;
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);
    }

    // -- getMember, catalog... (not API)

    public ObjectMember getMember(final Method method) {
        introspectUpTo(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        if (membersByMethod == null) {
            this.membersByMethod = catalogueMembers();
        }
        return membersByMethod.get(method);
    }

    private HashMap<Method, ObjectMember> catalogueMembers() {
        final HashMap<Method, ObjectMember> membersByMethod = _Maps.newHashMap();
        cataloguePropertiesAndCollections(membersByMethod);
        catalogueActions(membersByMethod);
        return membersByMethod;
    }

    private void cataloguePropertiesAndCollections(final Map<Method, ObjectMember> membersByMethod) {
        final Stream<ObjectAssociation> fields = streamAssociations(Contributed.EXCLUDED);
        fields.forEach(field->{
            final Stream<Facet> facets = field.streamFacets().filter(ImperativeFacet.PREDICATE);
            facets.forEach(facet->{
                final ImperativeFacet imperativeFacet = ImperativeFacet.Util.getImperativeFacet(facet);
                for (final Method imperativeFacetMethod : imperativeFacet.getMethods()) {
                    membersByMethod.put(imperativeFacetMethod, field);
                }
            });
        });
    }

    private void catalogueActions(final Map<Method, ObjectMember> membersByMethod) {
        final Stream<ObjectAction> userActions = streamObjectActions(Contributed.INCLUDED);
        userActions.forEach(userAction->{
            final Stream<Facet> facets = userAction.streamFacets().filter(ImperativeFacet.PREDICATE);
            facets.forEach(facet->{
                final ImperativeFacet imperativeFacet = ImperativeFacet.Util.getImperativeFacet(facet);
                for (final Method imperativeFacetMethod : imperativeFacet.getMethods()) {
                    membersByMethod.put(imperativeFacetMethod, userAction);
                }
            });
        });
    }
    
    // -- toString
    
    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("class", getFullIdentifier());
        str.append("type", getBeanSort().name());
        str.append("superclass", superclass() == null ? "Object" : superclass().getFullIdentifier());
        return str.toString();
    }

    // -- ELEMENT SPECIFICATION
    
    private final _Lazy<ObjectSpecification> elementSpecification = _Lazy.of(this::lookupElementSpecification); 
    
    @Override
    public ObjectSpecification getElementSpecification() {
        return elementSpecification.get();
    }
    
    private ObjectSpecification lookupElementSpecification() {
        return mapIfPresentElse(
                getFacet(TypeOfFacet.class), 
                typeOfFacet -> ElementSpecificationProvider.of(typeOfFacet).getElementType(), 
                null);
    }
    
    // --

}
