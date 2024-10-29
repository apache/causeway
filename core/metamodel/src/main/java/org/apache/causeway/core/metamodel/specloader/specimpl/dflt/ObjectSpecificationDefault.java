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
package org.apache.causeway.core.metamodel.specloader.specimpl.dflt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.config.beans.CausewayBeanTypeClassifier.Attributes;
import org.apache.causeway.core.metamodel.commons.ToString;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacetForStaticMemberName;
import org.apache.causeway.core.metamodel.facets.object.introspection.IntrospectionPolicyFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.IntrospectionState;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.causeway.core.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.causeway.core.metamodel.specloader.specimpl.FacetedMethodsBuilder;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectActionDefault;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.causeway.core.metamodel.specloader.specimpl.OneToManyAssociationDefault;
import org.apache.causeway.core.metamodel.specloader.specimpl.OneToOneAssociationDefault;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ObjectSpecificationDefault
extends ObjectSpecificationAbstract
implements FacetHolder {

    /**
     * Lazily built by {@link #getMember(Method)}.
     */
    private Map<ResolvedMethod, ObjectMember> membersByMethod = null;

    private final FacetedMethodsBuilder facetedMethodsBuilder;
    private final ClassSubstitutorRegistry classSubstitutorRegistry;

    @Getter(onMethod_ = {@Override})
    private final IntrospectionPolicy introspectionPolicy;

    public ObjectSpecificationDefault(
            final CausewayBeanMetaData typeMeta,
            final MetaModelContext mmc,
            final FacetProcessor facetProcessor,
            final PostProcessor postProcessor,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {

        super(typeMeta.getCorrespondingClass(),
                typeMeta.getLogicalType(),
                typeMeta.getLogicalType().getLogicalTypeSimpleName(),
                typeMeta.getBeanSort(), facetProcessor, postProcessor);

        this.isVetoedForInjection = typeMeta.getManagedBy().isVetoedForInjection();
        this.classSubstitutorRegistry = classSubstitutorRegistry;

        // must install EncapsulationFacet (if any) and MemberAnnotationPolicyFacet (if any)
        facetProcessor.processObjectType(typeMeta.getCorrespondingClass(), this);

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
        if(this.isValue()) {
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
        for (var interfaceType : interfaceTypes) {
            var interfaceSubstitute = classSubstitutorRegistry.getSubstitution(interfaceType);
            if (interfaceSubstitute.isReplace()) {
                var interfaceSpec = getSpecificationLoader().loadSpecification(interfaceSubstitute.getReplacement());
                interfaceSpecList.add(interfaceSpec);
            }
        }

        updateAsSubclassTo(interfaceSpecList);
        updateInterfaces(interfaceSpecList);
    }

    @Override
    protected void introspectMembers() {

        // yet this logic does not skip UNKNONW
        if(this.getBeanSort().isCollection()
                || this.getBeanSort().isVetoed()
                || this.isValue()) {
            if (log.isDebugEnabled()) {
                log.debug("skipping full introspection for {} type {}", this.getBeanSort(), getFullIdentifier());
            }
            return;
        }

        // create associations and actions
        replaceAssociations(createAssociations());
        replaceActions(createActions());

        postProcess();
    }

    private void addNamedFacetIfRequired() {
        if (getFacet(MemberNamedFacet.class) == null) {
            addFacet(new MemberNamedFacetForStaticMemberName(
                    _Strings.asNaturalName.apply(getShortIdentifier()),
                    this));
        }
    }

    // -- create associations and actions
    private Stream<ObjectAssociation> createAssociations() {
        return facetedMethodsBuilder.getAssociationFacetedMethods()
                .stream()
                .map(this::createAssociation)
                .filter(_NullSafe::isPresent);
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

    private Stream<ObjectAction> createActions() {
        return facetedMethodsBuilder.getActionFacetedMethods()
                .stream()
                .map(this::createAction)
                .filter(_NullSafe::isPresent);
    }

    private ObjectAction createAction(final FacetedMethod facetedMethod) {
        if (facetedMethod.getFeatureType().isAction()) {
            /* Assuming, that facetedMethod was already populated with ContributingFacet,
             * we copy the mixin-sort information from the FacetedMethod to the MixinFacet
             * that is held by the mixin's type spec. */
            mixinFacet()
            .flatMap(mixinFacet->_Casts.castTo(MixinFacetAbstract.class, mixinFacet))
            .ifPresent(mixinFacetAbstract->
                mixinFacetAbstract.initMixinSortFrom(facetedMethod));

            return this.isMixin()
                    ? ObjectActionDefault.forMixinMain(facetedMethod)
                    : ObjectActionDefault.forMethod(facetedMethod);
        } else {
            return null;
        }
    }

    // -- getObjectAction

    @Override
    public Optional<ObjectAction> getDeclaredAction(
            final @Nullable String id,
            final ImmutableEnumSet<ActionScope> actionScopes,
            final MixedIn mixedIn) {

        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED);

        return _Strings.isEmpty(id)
            ? Optional.empty()
            : streamDeclaredActions(actionScopes, mixedIn)
                .filter(action->
                    id.equals(action.getFeatureIdentifier().getMemberNameAndParameterClassNamesIdentityString())
                            || id.equals(action.getFeatureIdentifier().getMemberLogicalName())
                )
                .findFirst();
    }

    @Override
    public Optional<? extends ObjectMember> getMember(final ResolvedMethod method) {
        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED);

        if (membersByMethod == null) {
            this.membersByMethod = catalogueMembers();
        }

        var member = membersByMethod.get(method);
        return Optional.ofNullable(member);
    }

    private Map<ResolvedMethod, ObjectMember> catalogueMembers() {
        var membersByMethod = _Maps.<ResolvedMethod, ObjectMember>newHashMap();
        cataloguePropertiesAndCollections(membersByMethod::put);
        catalogueActions(membersByMethod::put);
        return membersByMethod;
    }

    private void cataloguePropertiesAndCollections(final BiConsumer<ResolvedMethod, ObjectMember> onMember) {
        streamDeclaredAssociations(MixedIn.EXCLUDED)
        .forEach(field->
            field.streamFacets(ImperativeFacet.class)
                .map(ImperativeFacet::getMethods)
                .flatMap(Can::stream)
                .map(MethodFacade::asMethodElseFail) // expected regular
                .peek(method->_Reflect.guardAgainstSynthetic(method.method())) // expected non-synthetic
                .forEach(imperativeFacetMethod->onMember.accept(imperativeFacetMethod, field)));
    }

    private void catalogueActions(final BiConsumer<ResolvedMethod, ObjectMember> onMember) {
        streamDeclaredActions(MixedIn.INCLUDED)
        .forEach(userAction->
            userAction.streamFacets(ImperativeFacet.class)
                .map(ImperativeFacet::getMethods)
                .flatMap(Can::stream)
                .map(MethodFacade::asMethodForIntrospection)
                .peek(method->_Reflect.guardAgainstSynthetic(method.method())) // expected non-synthetic
                .forEach(imperativeFacetMethod->
                    onMember.accept(imperativeFacetMethod, userAction)));
    }

    // -- ELEMENT SPECIFICATION

    private final _Lazy<Optional<ObjectSpecification>> elementSpecification =
            _Lazy.threadSafe(()->lookupFacet(TypeOfFacet.class)
                    .map(typeOfFacet -> typeOfFacet.elementSpec()));

    @Override
    public Optional<ObjectSpecification> getElementSpecification() {
        return elementSpecification.get();
    }

    // -- TABLE COLUMN RENDERING

    @Override
    public final Stream<ObjectAssociation> streamAssociationsForColumnRendering(
            final Identifier memberIdentifier,
            final ManagedObject parentObject) {

        return new _MembersAsColumns(getMetaModelContext())
            .streamAssociationsForColumnRendering(this, memberIdentifier, parentObject);
    }

    @Override
    public Stream<ObjectAction> streamActionsForColumnRendering(
            final Identifier memberIdentifier) {
        return new _MembersAsColumns(getMetaModelContext())
                .streamActionsForColumnRendering(this, memberIdentifier);
    }

    // -- DETERMINE INJECTABILITY

    private boolean isVetoedForInjection;

    private _Lazy<Boolean> isInjectableLazy = _Lazy.threadSafe(()->
        !isVetoedForInjection
                && !getBeanSort().isAbstract()
                && !getBeanSort().isValue()
                && !getBeanSort().isEntity()
                && !getBeanSort().isViewModel()
                && !getBeanSort().isMixin()
                && (getBeanSort().isManagedBeanAny()
                        || getServiceRegistry()
                                .lookupRegisteredBeanById(getLogicalType())
                                .isPresent())
                );

    @Override
    public boolean isInjectable() {
        return isInjectableLazy.get();
    }

    private _Lazy<Boolean> isDomainServiceLazy = _Lazy.threadSafe(()->
        Attributes.HAS_DOMAIN_SERVICE_SEMANTICS.lookup(_ClassCache.getInstance(), getCorrespondingClass())
            .map("true"::equals)
            .orElse(false));

    @Override
    public boolean isDomainService() {
        return isDomainServiceLazy.get();
    }

    // -- TO STRING

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("class", getFullIdentifier());
        str.append("type", getBeanSort().name());
        str.append("superclass", superclass() == null ? "Object" : superclass().getFullIdentifier());
        return str.toString();
    }

}
