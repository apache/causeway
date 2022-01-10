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
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacetForStaticMemberName;
import org.apache.isis.core.metamodel.facets.object.introspection.IntrospectionPolicyFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.ElementSpecificationProvider;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.isis.core.metamodel.specloader.specimpl.FacetedMethodsBuilder;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionDefault;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToManyAssociationDefault;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToOneAssociationDefault;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ObjectSpecificationDefault
extends ObjectSpecificationAbstract
implements FacetHolder {

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

    private final ClassSubstitutorRegistry classSubstitutorRegistry;

    @Getter(onMethod_ = {@Override})
    private final IntrospectionPolicy introspectionPolicy;

    /**
     * available only for managed-beans
     */
    private final String nameIfIsManagedBean;

    public ObjectSpecificationDefault(
            final Class<?> correspondingClass,
            final BeanSort beanSort,
            final MetaModelContext mmc,
            final FacetProcessor facetProcessor,
            final String nameIfIsManagedBean,
            final PostProcessor postProcessor,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {

        super(correspondingClass, determineShortName(correspondingClass), beanSort, facetProcessor, postProcessor);

        this.nameIfIsManagedBean = nameIfIsManagedBean;
        this.classSubstitutorRegistry = classSubstitutorRegistry;

        // must install EncapsulationFacet (if any) and MemberAnnotationPolicyFacet (if any)
        facetProcessor.processObjectType(correspondingClass, this);

        // naturally supports attribute inheritance from the type's hierarchy
        final IntrospectionPolicy introspectionPolicy =
                this.lookupFacet(IntrospectionPolicyFacet.class)
                .map(introspectionPolicyFacet->
                        introspectionPolicyFacet
                        .getIntrospectionPolicy(mmc.getConfiguration()))
                .orElseGet(()->mmc.getConfiguration().getCore().getMetaModel().getIntrospector().getPolicy());

        this.introspectionPolicy = introspectionPolicy;

        this.facetedMethodsBuilder =
                new FacetedMethodsBuilder(this, facetProcessor, classSubstitutorRegistry);

    }

    @Override
    protected void introspectTypeHierarchy() {

        facetedMethodsBuilder.introspectClass();

        // name
        addNamedFacetIfRequired();

        // go no further if a value
        if(this.containsFacet(ValueFacet.class)) {
            if (log.isDebugEnabled()) {
                log.debug("skipping type hierarchy introspection for value type {}", getFullIdentifier());
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
        for (val interfaceType : interfaceTypes) {
            val interfaceSubstitute = classSubstitutorRegistry.getSubstitution(interfaceType);
            if (interfaceSubstitute.isReplace()) {
                val interfaceSpec = getSpecificationLoader().loadSpecification(interfaceSubstitute.getReplacement());
                interfaceSpecList.add(interfaceSpec);
            }
        }

        updateAsSubclassTo(interfaceSpecList);
        updateInterfaces(interfaceSpecList);
    }

    @Override
    protected void introspectMembers() {

        if(this.containsFacet(ValueFacet.class)) {
            if (log.isDebugEnabled()) {
                log.debug("skipping full introspection for value type {}", getFullIdentifier());
            }
            return;
        }

        // associations and actions
        val associations = createAssociations();
        sortAndUpdateAssociations(associations);

        val actions = createActions();
        sortCacheAndUpdateActions(actions);

        postProcess();
    }

    private void addNamedFacetIfRequired() {
        if (getFacet(MemberNamedFacet.class) == null) {
            addFacet(new MemberNamedFacetForStaticMemberName(
                    StringExtensions.asNaturalName2(getShortIdentifier()),
                    this));
        }
    }


    // -- create associations and actions
    private List<ObjectAssociation> createAssociations() {
        val associations = _Lists.<ObjectAssociation>newArrayList();
        val associationFacetedMethods =
                facetedMethodsBuilder.getAssociationFacetedMethods();
        for (val facetedMethod : associationFacetedMethods) {
            val association = createAssociation(facetedMethod);
            if(association != null) {
                associations.add(association);
            }
        }
        return associations;
    }

    private ObjectAssociation createAssociation(final FacetedMethod facetMethod) {
        if (facetMethod.getFeatureType().isCollection()) {
            return OneToManyAssociationDefault.forMethod(facetMethod);
        } else if (facetMethod.getFeatureType().isProperty()) {
            return OneToOneAssociationDefault.forMethod(facetMethod);
        } else {
            return null;
        }
    }

    private List<ObjectAction> createActions() {
        val actions = _Lists.<ObjectAction>newArrayList();
        for (val facetedMethod : facetedMethodsBuilder.getActionFacetedMethods()) {
            val action = createAction(facetedMethod);
            if(action != null) {
                actions.add(action);
            }
        }
        return actions;
    }


    private ObjectAction createAction(final FacetedMethod facetedMethod) {
        if (facetedMethod.getFeatureType().isAction()) {
            return this.isMixin()
                    ? ObjectActionDefault.forMixinMain(facetedMethod)
                    : ObjectActionDefault.forMethod(facetedMethod);
        } else {
            return null;
        }
    }

    // -- PREDICATES

    @Override
    public String getManagedBeanName() {
        return nameIfIsManagedBean;
    }

    // -- getObjectAction

    @Override
    public Optional<ObjectAction> getDeclaredAction(
            final @Nullable String id,
            final ImmutableEnumSet<ActionScope> actionScopes,
            final MixedIn mixedIn) {

        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED);

        return id == null
            ? Optional.empty()
            : streamDeclaredActions(actionScopes, mixedIn)
                .filter(action->
                    id.equals(action.getFeatureIdentifier().getMemberNameAndParameterClassNamesIdentityString())
                            || id.equals(action.getFeatureIdentifier().getMemberLogicalName())
                )
                .findFirst();
    }

    @Override
    public Optional<? extends ObjectMember> getMember(final Method method) {
        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED);

        if (membersByMethod == null) {
            this.membersByMethod = catalogueMembers();
        }

        val member = membersByMethod.get(method);
        return Optional.ofNullable(member);
    }

    private Map<Method, ObjectMember> catalogueMembers() {
        val membersByMethod = _Maps.<Method, ObjectMember>newHashMap();
        cataloguePropertiesAndCollections(membersByMethod::put);
        catalogueActions(membersByMethod::put);
        postprocessSyntheticMembers(membersByMethod);
        return membersByMethod;
    }

    private void cataloguePropertiesAndCollections(final BiConsumer<Method, ObjectMember> onMember) {
        streamDeclaredAssociations(MixedIn.EXCLUDED)
        .forEach(field->
            field.streamFacets(ImperativeFacet.class)
                .map(ImperativeFacet::getMethods)
                .flatMap(Can::stream)
                .forEach(imperativeFacetMethod->onMember.accept(imperativeFacetMethod, field)));
    }

    private void catalogueActions(final BiConsumer<Method, ObjectMember> onMember) {
        streamDeclaredActions(MixedIn.INCLUDED)
        .forEach(userAction->
            userAction.streamFacets(ImperativeFacet.class)
                .map(ImperativeFacet::getMethods)
                .flatMap(Can::stream)
                .forEach(imperativeFacetMethod->
                onMember.accept(imperativeFacetMethod, userAction)));
    }

    /**
     * for any synthetic method also add an entry with its regular method,
     * as found in the method's declaring class type-hierarchy
     */
    private void postprocessSyntheticMembers(final HashMap<Method, ObjectMember> membersByMethod) {
        val syntheticEntries = Can.ofStream(
            membersByMethod
            .entrySet()
            .stream()
            .filter(entry->entry.getKey().isSynthetic()));

        syntheticEntries
        .forEach(entry->{
            val objectMember = entry.getValue();
            val syntheticMethod = entry.getKey();
            _Reflect
            .lookupRegularMethodForSynthetic(syntheticMethod)
            .ifPresent(regularMethod->
                membersByMethod.computeIfAbsent(regularMethod, key->objectMember));
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

    private final _Lazy<Optional<ObjectSpecification>> elementSpecification = _Lazy.of(this::lookupElementSpecification);

    @Override
    public Optional<ObjectSpecification> getElementSpecification() {
        return elementSpecification.get();
    }

    private Optional<ObjectSpecification> lookupElementSpecification() {
        return Optional.ofNullable(getFacet(TypeOfFacet.class))
                .map(typeOfFacet -> ElementSpecificationProvider.of(typeOfFacet).getElementType());
    }

    // -- TABLE COLUMN RENDERING

    @Override
    public final Stream<OneToOneAssociation> streamPropertiesForColumnRendering(
            final Identifier memberIdentifier,
            final ManagedObject parentObject) {

        return new _PropertiesAsColumns(getMetaModelContext())
            .streamPropertiesForColumnRendering(this, memberIdentifier, parentObject);
    }


}
