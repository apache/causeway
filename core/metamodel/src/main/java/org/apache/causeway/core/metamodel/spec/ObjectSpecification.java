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
package org.apache.causeway.core.metamodel.spec;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.exceptions.UnrecoverableException;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.id.HasLogicalType;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Streams;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.commons.ClassExtensions;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.InteractionResult;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.HasFacetHolder;
import org.apache.causeway.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.help.HelpFacet;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.all.i8n.noun.HasNoun;
import org.apache.causeway.core.metamodel.facets.all.i8n.staatic.HasStaticText;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.causeway.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet.Contributing;
import org.apache.causeway.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionContext;
import org.apache.causeway.core.metamodel.interactions.ObjectTitleContext;
import org.apache.causeway.core.metamodel.interactions.ObjectValidityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.MixedInAction;
import org.apache.causeway.core.metamodel.spec.feature.MixedInMember;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionContainer;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

import org.jspecify.annotations.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Represents any domain object, abstract type, bean or value,
 * and uniquely corresponds to a {@link java.lang.Class}).
 * <p>
 * As specifications are potentially cyclic (specifically a class will reference its
 * subclasses, which in turn reference their superclass) they need be created
 * first, and then later work out its internals. Hence we create
 * {@link ObjectSpecification}(s) as we need them, then introspect them later.
 */
public interface ObjectSpecification
extends
    Specification,
    HasLogicalType,
    HasFacetHolder,
    Hierarchical,
    ObjectActionContainer,
    ObjectAssociationContainer,
    Comparable<ObjectSpecification> {

    @UtilityClass
    class Comparators{

        public final Comparator<ObjectSpecification> BY_BEANSORT_THEN_LOGICALTYPE =
                Comparator.comparing(ObjectSpecification::getBeanSort)
                    .thenComparing(ObjectSpecification::logicalType);

        public final Comparator<ObjectSpecification> FULLY_QUALIFIED_CLASS_NAME =
                Comparator.comparing(ObjectSpecification::getFullIdentifier);

        public final Comparator<ObjectSpecification> SHORT_IDENTIFIER_IGNORE_CASE =
                (final ObjectSpecification s1, final ObjectSpecification s2) ->
            s1.getShortIdentifier().compareToIgnoreCase(s2.getShortIdentifier());
    }

    IntrospectionPolicy getIntrospectionPolicy();

    /**
     * Natural order, that is, by {@link BeanSort} then by {@link LogicalType}.
     */
    @Override
    default int compareTo(final ObjectSpecification o) {
        return Comparators.BY_BEANSORT_THEN_LOGICALTYPE.compare(this, o);
    }

    /**
     * @param memberId
     * @return optionally the ObjectMember associated with given {@code memberId},
     * based on whether given memberId exists
     */
    Optional<? extends ObjectMember> getMember(String memberId);

    default ObjectMember getMemberElseFail(final String memberId) {
        return getMember(memberId).orElseThrow(()->{
            var msg = "Member '" + memberId + "' does not correspond "
                    + "to any of the object's fields or actions.";
            return new UnsupportedOperationException(msg);
        });
    }

    /**
     * @param method
     * @return optionally the ObjectMember associated with given {@code method},
     * based on whether such an association exists
     */
    Optional<? extends ObjectMember> getMember(ResolvedMethod method);

    default ObjectMember getMemberElseFail(final @NonNull ResolvedMethod method) {
        return getMember(method).orElseThrow(()->{
            var methodName = method.name();
            var msg = "Method '" + methodName + "' does not correspond "
                    + "to any of the object's fields or actions.";
            return new UnsupportedOperationException(msg);
        });
    }

    /**
     * @since 2.0
     */
    default Optional<MixedInMember> lookupMixedInMember(final ObjectSpecification mixinSpec) {
        return Stream.concat(
                streamAnyActions(MixedIn.INCLUDED),
                streamAssociations(MixedIn.INCLUDED))
                .filter(MixedInMember.class::isInstance)
                .map(MixedInMember.class::cast)
                .filter(member->member.getMixinType().getFeatureIdentifier().equals(mixinSpec.getFeatureIdentifier()))
                .findAny();
    }

    /**
     * @since 2.0
     */
    default Optional<MixedInAction> lookupMixedInAction(final ObjectSpecification mixinSpec) {
        return streamAnyActions(MixedIn.INCLUDED)
            .filter(MixedInAction.class::isInstance)
            .map(MixedInAction.class::cast)
            .filter(member->member.getMixinType().getFeatureIdentifier().equals(mixinSpec.getFeatureIdentifier()))
            .findAny();
    }

    /**
     * @return Java class this specification is associated with
     */
    Class<?> getCorrespondingClass();

    /**
     * Returns an (immutable) "full" identifier for this specification.
     * <p>
     * This will be the fully qualified name of the Class object that this
     * object represents (i.e. it includes the package name).
     */
    String getFullIdentifier();

    /**
     * Returns an (immutable) "short" identifier for this specification.
     * <p>
     * This will be the class name without the package; any text up to and
     * including the last period is removed.
     */
    String getShortIdentifier();

    /**
     * Immutable set of {@link LogicalType} aliases for corresponding
     * domain object or service.
     * <p>
     * Corresponds to {@link DomainService#aliased()} and
     * {@link DomainObject#aliased()}.
     */
    Can<LogicalType> getAliases();

    /**
     * Returns the (singular) name for objects of this specification.
     * <p>
     * Corresponds to the {@link HasNoun#translated()}
     * of {@link ObjectNamedFacet}; is
     * not necessarily immutable.
     */
    String getSingularName();

    /**
     * Returns the description, if any, of the specification.
     *
     * <p>
     * Corresponds to the {@link HasStaticText#translated() value} of
     * {@link ObjectDescribedFacet}; is not necessarily immutable.
     */
    String getDescription();

    /**
     * Returns a help string or lookup reference, if any, of the specification.
     *
     * <p>
     * Corresponds to the {@link HelpFacet#value() value} of {@link HelpFacet};
     * is not necessarily immutable.
     */
    String getHelp();

    /**
     * Returns the title to display of target adapter, rendered within the context
     * of some other adapter (if any).
     *
     * <p>
     * @see TitleFacet#title(TitleRenderRequest)
     */
    String getTitle(TitleRenderRequest titleRenderRequest);

    /**
     * Returns the name of an icon to use for the specified object.
     *
     * <p>
     * Corresponds to the {@link IconFacet#iconName(ManagedObject) icon name}
     * returned by the {@link IconFacet}; is not necessarily immutable.
     */
    String getIconName(ManagedObject object);

    ObjectIcon getIcon(ManagedObject object);

    /**
     * Returns this object's navigable parent, if any.
     * @param object
     * @since 2.0
     */
    Object getNavigableParent(Object object);

    /**
     * Returns the CSS class name to use for the specified object.
     *
     * <p>
     * Corresponds to the {@link CssClassFacet#cssClass(ManagedObject)} value}
     * returned by the {@link CssClassFacet}.
     *
     * @param objectAdapter - to evaluate (may be <tt>null</tt> if called by deprecated {@link #getCssClass}).
     */
    String getCssClass(ManagedObject domainObject);

    Optional<FontAwesomeLayers> getFaLayers(ManagedObject domainObject);

    /**
     * @return optionally the element type spec based on presence of the TypeOfFacet
     * @since 2.0
     */
    Optional<ObjectSpecification> getElementSpecification();

    /**
     * @since 2.0
     */
    BeanSort getBeanSort();

    /**
     * Optionally the mixin sort {@link Contributing},
     * based on whether the corresponding class is a mixin type.
     * @since 2.0
     */
    Optional<Contributing> contributing();

    // //////////////////////////////////////////////////////////////
    // TitleContext
    // //////////////////////////////////////////////////////////////

    /**
     * Create an {@link InteractionContext} representing an attempt to read the
     * object's title.
     */
    ObjectTitleContext createTitleInteractionContext(
            ManagedObject targetObjectAdapter,
            InteractionInitiatedBy invocationMethod);

    // //////////////////////////////////////////////////////////////
    // ValidityContext, Validity
    // //////////////////////////////////////////////////////////////

    // internal API
    ObjectValidityContext createValidityInteractionContext(
            final ManagedObject targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Determines whether the specified object is in a valid state (for example,
     * so can be persisted); represented as a {@link Consent}.
     */
    Consent isValid(
            final ManagedObject targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Determines whether the specified object is in a valid state (for example,
     * so can be persisted); represented as a {@link InteractionResult}.
     */
    InteractionResult isValidResult(
            final ManagedObject targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy);

    // //////////////////////////////////////////////////////////////
    // Facets
    // //////////////////////////////////////////////////////////////

    /**
     * Determines if the object represents an value or object.
     *
     * <p>
     * In effect, means that it doesn't have the {@link CollectionFacet}, and
     * therefore will return NOT {@link #isPlural()}
     *
     * @see #isPlural()
     */
    default boolean isSingular() {
        return !isPlural();
    }

    /**
     * Determines if objects of this type are a parented (internal) or free-standing (external) collection.
     *
     * <p>
     * In effect, means has got {@link CollectionFacet}, and therefore will
     * return NOT {@link #isSingular()}.
     *
     * @see #isSingular()
     */
    default boolean isPlural() {
        return getBeanSort().isCollection();
    }

    /**
     * Determines if objects of this type are values.
     *
     * <p>
     * In effect, means has got {@link ValueFacet}.
     */
    default boolean isValue() {
        return getBeanSort().isValue()
                || valueFacet().isPresent();
    }

    /**
     * Whether objects of this type are composite values.
     */
    default boolean isCompositeValue() {
        return getBeanSort().isValue()
                && valueFacet().map(ValueFacet::isCompositeValueType).orElse(false);
    }

    /**
     * Determines if objects of this type are parented (a parented collection, or an aggregated entity).
     *
     * <p>
     * In effect, means has got {@link ParentedCollectionFacet}.
     */
    boolean isParented();

    /**
     * Determines if objects of this type are either values or aggregated.
     *
     * @see #isValue()
     * @see #isParented()
     */
    default boolean isValueOrIsParented() {
        return isValue() || isParented();
    }

    /**
     * Whether has the {@link ImmutableFacet}.
     */
    boolean isImmutable();

    /**
     * Whether has the {@link HiddenFacet}
     */
    boolean isHidden();

    /**
     * Whether represents a bean, that is a managed bean
     * with scoped life-cycle, available for dependency injection.
     * <p>
     * DANGER: don't call during MM introspection
     * @apiNote this predicate might not be valid until all services
     *      have been discovered and the application context has settled
     */
    boolean isInjectable();

    /**
     * Whether represents a bean, that is in effect annotated with {@link DomainService}.
     */
    boolean isDomainService();

    default boolean isMixin() {
        return getBeanSort().isMixin();
    }

    /**
     * Whether {@link #getCorrespondingClass()} is {@link Void} or {@code void}.
     */
    default boolean isVoid() {
        return getCorrespondingClass()==void.class
                || getCorrespondingClass()==Void.class;
    }

    /**
     * Whether {@link #getCorrespondingClass()} is {@code void} (but not {@link Void}).
     */
    default boolean isVoidPrimitive() {
        return getCorrespondingClass()==void.class;
    }

    /**
     * Whether {@link #getCorrespondingClass()} is a primitive type,
     * but not {@link Void} or {@code void}.
     */
    default boolean isPrimitive() {
        return !isVoid()
                && getCorrespondingClass().isPrimitive();
    }

    default boolean isAbstract() {
        return getBeanSort().isAbstract();
    }

    /**
    * Whether {@link #getCorrespondingClass()} implements {@link Comparable}
    * or has ordering (primitives, strings and enums).
    */
    default boolean isComparableOrOrdered() {
        var cls = getCorrespondingClass();
        return Comparable.class.isAssignableFrom(cls)
                || cls.isPrimitive()
                || cls.equals(String.class)
                || cls.isEnum();
    }

    /**
     * Includes abstract types that have {@link EntityFacet}.
     */
    default boolean isEntity() {
        return getBeanSort().isEntity()
                || (getBeanSort().isAbstract()
                        && entityFacet().isPresent());
    }

    /**
     * Includes abstract types that have {@link ViewModelFacet}.
     */
    default boolean isViewModel() {
        return getBeanSort().isViewModel()
                || (getBeanSort().isAbstract()
                        && viewmodelFacet().isPresent());
    }

    /**
     * Includes abstract types that have
     * {@link ViewModelFacet} or {@link EntityFacet}.
     *
     * @see #isViewModel()
     * @see #isEntity()
     */
    default boolean isEntityOrViewModel() {
        return isViewModel() || isEntity();
    }

    /**
     * @see #isViewModel()
     * @see #isValue()
     */
    default boolean isViewModelOrValue() {
        return isViewModel() || isValue();
    }

    /**
     * @see #isViewModel()
     * @see #isValue()
     * @see #isVoid()
     */
    default boolean isViewModelOrValueOrVoid() {
        return isViewModel() || isValue() || isVoid();
    }

    /**
     * @see #getBeanSort()
     */
    default boolean isEntityOrViewModelOrAbstract() {
        // optimized, no need to check facets
        return getBeanSort().isViewModel()
                || getBeanSort().isEntity()
                || getBeanSort().isAbstract();
    }

    /**
     * @since 2.0
     */
    default Object instantiatePojo() {
        final Class<?> correspondingClass = getCorrespondingClass();
        if (correspondingClass.isArray()) {
            return Array.newInstance(correspondingClass.getComponentType(), 0);
        }

        final Class<?> cls = correspondingClass;
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new UnrecoverableException("Cannot create an instance of an abstract class: " + cls);
        }

        final Object newInstance;
        try {
            newInstance = cls.getDeclaredConstructor().newInstance();
        } catch (final Throwable e) {
            throw new UnrecoverableException("Failed to create instance of type " + getFullIdentifier(), e);
        }

        return newInstance;
    }

    /**
     * Streams all FacetHolders associated with this spec. (including inherited)
     * @since 2.0
     */
    default Stream<FacetHolder> streamFacetHolders(){
        var self = Stream.of(this);
        var actions = streamAnyActions(MixedIn.EXCLUDED);
        var actionParameters = streamAnyActions(MixedIn.EXCLUDED)
                .flatMap(action->action.getParameterCount()>0
                        ? action.getParameters().stream()
                        : Stream.empty());
        var properties = streamProperties(MixedIn.EXCLUDED);
        var collections = streamCollections(MixedIn.EXCLUDED);

        return _Streams.concat(self, actions, actionParameters, properties, collections);
    }

    /**
     * @return whether the corresponding type can be mapped onto a REFERENCE (schema) or an Oid,
     * that is the type is 'identifiable' (aka 'referencable' or 'bookmarkable')
     * @since 2.0
     */
    default boolean isIdentifiable() {
        return isInjectable() || isViewModel() || isEntity();
    }

    /**
     * Delegates to {@link ObjectManager#createObject(ObjectSpecification)}
     * @since 2.0
     */
    default ManagedObject createObject() {
        var managedObject = getObjectManager().createObject(this);
        return managedObject;
    }

    // -- TYPE COMPATIBILITY UTILITIES

    default public void assertPojoCompatible(final @Nullable Object pojo) {

        // can do this check only when the pojo is not null, otherwise is always considered valid
        if(pojo==null) return;

        if(!isPojoCompatible(pojo)) {
            var expectedType = getCorrespondingClass();
            throw _Exceptions.illegalArgument(
                    "Pojo not compatible with ObjectSpecification, " +
                    "objectSpec.correspondingClass = %s, " +
                    "pojo.getClass() = %s, " +
                    "pojo.toString() = %s",
                    expectedType, pojo.getClass(), pojo.toString());
        }
    }

    default public boolean isAssignableFrom(final Class<?> actualType) {
        var expectedType = getCorrespondingClass();
        if(expectedType.isAssignableFrom(actualType)
                || ClassExtensions.equalsWhenBoxing(expectedType, actualType)) {
            return true;
        }
        return false;
    }

    default public boolean isPojoCompatible(final Object pojo) {
        if(pojo==null)  return true;

        var expectedType = getCorrespondingClass();
        var actualType = pojo.getClass();

        if(expectedType.isAssignableFrom(actualType)
                || ClassExtensions.equalsWhenBoxing(expectedType, actualType)) {
            return true;
        }

        var elementSpec = getElementSpecification()
                .orElse(this);
        return _NullSafe.streamAutodetect(pojo)
                .filter(element->!Objects.equals(element, pojo)) // to prevent infinite recursion depth
                .allMatch(elementSpec::isPojoCompatible);
    }

    /**
     * @return whether corresponding class implements {@link java.io.Serializable}.
     * @apiNote: per se does not tell what recreation strategy to use, the corresponding class
     * might be an entity or a view-model or a value with eg. encodable semantics, which have
     * different object recreation mechanics
     * @since 2.0.0
     */
    default boolean isSerializable() {
        return Serializable.class.isAssignableFrom(getCorrespondingClass());
    }

    default String fqcn() {
        return getCorrespondingClass().getName();
    }

    default Stream<ObjectSpecification> streamTypeHierarchy() {
        return superclass()!=null
                ? Stream.concat(Stream.of(this), superclass().streamTypeHierarchy())
                : Stream.of(this);
    }

    // -- COMMON SUPER TYPE FINDER

    /**
     * Lowest common ancestor search within the combined type hierarchy.
     */
    public static ObjectSpecification commonSuperType(
            final @NonNull ObjectSpecification a,
            final @NonNull ObjectSpecification b) {

        var cls_a = a.getCorrespondingClass();
        var cls_b = b.getCorrespondingClass();
        if(cls_a.isAssignableFrom(cls_b)) {
            return a;
        }
        if(cls_b.isAssignableFrom(cls_a)) {
            return b;
        }
        // assuming the algorithm is correct: if non of the above is true,
        // we must be able to walk up the tree on both branches
        _Assert.assertNotNull(a.superclass());
        _Assert.assertNotNull(b.superclass());
        return commonSuperType(a.superclass(), b.superclass());
    }

    // -- VALUE SEMANTICS SUPPORT

    /** introduced for lookup optimization / allow memoization */
    Optional<ValueFacet<?>> valueFacet();
    default ValueFacet<?> valueFacetElseFail() {
        return valueFacet().orElseThrow(()->
            _Exceptions.unrecoverable("Value type %s must have a ValueFacet", toString()));
    }

    /** introduced for lookup optimization / allow memoization */
    Optional<EntityFacet> entityFacet();
    default EntityFacet entityFacetElseFail() {
        return entityFacet().orElseThrow(()->
            _Exceptions.unrecoverable("Entity type %s must have an EntityFacet", toString()));
    }

    /** introduced for lookup optimization / allow memoization */
    Optional<ViewModelFacet> viewmodelFacet();
    default ViewModelFacet viewmodelFacetElseFail() {
        return viewmodelFacet().orElseThrow(()->
            _Exceptions.unrecoverable("ViewModel type %s must have a ViewModelFacet", toString()));
    }

    /** introduced for lookup optimization / allow memoization */
    Optional<MixinFacet> mixinFacet();
    default MixinFacet mixinFacetElseFail() {
        return mixinFacet().orElseThrow(()->
            _Exceptions.unrecoverable("Type %s has BeanSort MIXIN but ended up NOT having a MixinFacet", toString()));
    }

}
