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
package org.apache.isis.core.metamodel.spec;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.applib.id.HasLogicalType;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Streams;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.help.HelpFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.i8n.noun.HasNoun;
import org.apache.isis.core.metamodel.facets.all.i8n.noun.NounForm;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.HasStaticText;
import org.apache.isis.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFactory;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.interactions.InteractionContext;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.create.ObjectCreator;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationContainer;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.core.metamodel.specloader.specimpl.MixedInMember;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;

import lombok.NonNull;
import lombok.val;

/**
 * Represents an entity or value (cf {@link java.lang.Class}) within the
 * metamodel.
 *
 * <p>
 * As specifications are cyclic (specifically a class will reference its
 * subclasses, which in turn reference their superclass) they need be created
 * first, and then later work out its internals. Hence we create
 * {@link ObjectSpecification}s as we need them, and then introspect them later.
 */
public interface ObjectSpecification
extends
    Specification,
    ObjectActionContainer,
    ObjectAssociationContainer,
    Hierarchical,
    DefaultProvider,
    HasLogicalType {

    final class Comparators{
        private Comparators(){}

        public static final Comparator<ObjectSpecification> FULLY_QUALIFIED_CLASS_NAME =
                (final ObjectSpecification o1, final ObjectSpecification o2) ->
        o1.getFullIdentifier().compareTo(o2.getFullIdentifier());

        public static final Comparator<ObjectSpecification> SHORT_IDENTIFIER_IGNORE_CASE =
                (final ObjectSpecification s1, final ObjectSpecification s2) ->
        s1.getShortIdentifier().compareToIgnoreCase(s2.getShortIdentifier());
    }

    /**
     * @param memberId
     * @return optionally the ObjectMember associated with given {@code memberId},
     * based on whether given memberId exists
     */
    Optional<? extends ObjectMember> getMember(String memberId);

    default ObjectMember getMemberElseFail(final String memberId) {
        return getMember(memberId).orElseThrow(()->{
            val msg = "Member '" + memberId + "' does not correspond "
                    + "to any of the object's fields or actions.";
            return new UnsupportedOperationException(msg);
        });
    }

    /**
     * @param method
     * @return optionally the ObjectMember associated with given {@code method},
     * based on whether such an association exists
     */
    Optional<? extends ObjectMember> getMember(Method method);

    default ObjectMember getMemberElseFail(final @NonNull Method method) {
        return getMember(method).orElseThrow(()->{
            val methodName = method.getName();
            val msg = "Method '" + methodName + "' does not correspond "
                    + "to any of the object's fields or actions.";
            return new UnsupportedOperationException(msg);
        });
    }

    /**
     * @since 2.0
     */
    public default Optional<MixedInMember> lookupMixedInMember(final ObjectSpecification mixinSpec) {
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
    public default Optional<ObjectActionMixedIn> lookupMixedInAction(final ObjectSpecification mixinSpec) {
        return
                streamAnyActions(MixedIn.INCLUDED)
                .filter(ObjectActionMixedIn.class::isInstance)
                .map(ObjectActionMixedIn.class::cast)
                .filter(member->member.getMixinType().getFeatureIdentifier().equals(mixinSpec.getFeatureIdentifier()))
                .findAny();
    }


    /**
     * @return Java class this specification is associated with
     */
    Class<?> getCorrespondingClass();

    /**
     * Returns an (immutable) "full" identifier for this specification.
     *
     * <p>
     * This will be the fully qualified name of the Class object that this
     * object represents (i.e. it includes the package name).
     */
    String getFullIdentifier();

    /**
     * Returns an (immutable) "short" identifier for this specification.
     *
     * <p>
     * This will be the class name without the package; any text up to and
     * including the last period is removed.
     */
    String getShortIdentifier();

    /**
     * Returns the (singular) name for objects of this specification.
     * <p>
     * Corresponds to the {@link HasNoun#translated(NounForm)}
     * with {@link NounForm#SINGULAR}
     * of {@link ObjectNamedFacet}; is
     * not necessarily immutable.
     */
    String getSingularName();

    /**
     * Returns the plural name for objects of this specification.
     * Corresponds to the {@link HasNoun#translated(NounForm)}
     * with {@link NounForm#PLURAL}
     * of {@link ObjectNamedFacet}; is
     * not necessarily immutable.
     */
    String getPluralName();

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
    String getCssClass(ManagedObject objectAdapter);

    Optional<CssClassFaFactory> getCssClassFaFactory();

    /**
     * @return optionally the element type spec based on presence of the TypeOfFacet
     * @since 2.0
     */
    Optional<ObjectSpecification> getElementSpecification();

    /**
     *
     * @since 2.0
     */
    BeanSort getBeanSort();

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
     * therefore will return NOT {@link #isNonScalar()}
     *
     * @see #isNonScalar()
     */
    default boolean isScalar() {
        return !isNonScalar();
    }

    /**
     * Determines if objects of this type are a parented (internal) or free-standing (external) collection.
     *
     * <p>
     * In effect, means has got {@link CollectionFacet}, and therefore will
     * return NOT {@link #isScalar()}.
     *
     * @see #isScalar()
     */
    default boolean isNonScalar() {
        return getBeanSort().isCollection();
    }

    /**
     * Determines if objects of this type are values.
     *
     * <p>
     * In effect, means has got {@link ValueFacet}.
     */
    default boolean isValue() {
        return getBeanSort().isValue();
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
     * Determines if objects of this type can be converted to a data-stream.
     *
     * <p>
     * In effect, means has got {@link EncodableFacet}.
     */
    boolean isEncodeable();

    /**
     * Whether has the {@link ImmutableFacet}.
     */
    boolean isImmutable();

    /**
     * Whether has the {@link HiddenFacet}
     */
    boolean isHidden();

    /**
     * Whether this specification represents a bean, that is a managed bean
     * with scoped life-cycle, available for dependency injection.
     */
    default boolean isManagedBean() {
        return getManagedBeanName()!=null;
    }

    /**
     * If this specification represents a bean, that is a managed bean, then
     * returns the bean's name/id as recognized by the IoC container.
     * <p>Otherwise returns {@code null}.
     */
    String getManagedBeanName();

    default boolean isMixin() {
        return getBeanSort().isMixin();
    }

    //TODO this predicate can now be answered by getBeanSort().isAbstract(), we can retire any old logic
    boolean isAbstract();

    default boolean isEntity() {
        return getBeanSort().isEntity()
                || (getBeanSort().isAbstract()
                        && lookupFacet(EntityFacet.class).isPresent());
    }

    default boolean isViewModel() {
        return getBeanSort().isViewModel()
                || (getBeanSort().isAbstract()
                        && lookupFacet(ViewModelFacet.class).isPresent());
    }

    default boolean isEntityOrViewModel() {
        return isViewModel() || isEntity();
    }

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

        val self = Stream.of(this);
        val actions = streamAnyActions(MixedIn.EXCLUDED);
        val actionParameters = streamAnyActions(MixedIn.EXCLUDED)
                .flatMap(action->action.getParameterCount()>0
                        ? action.getParameters().stream()
                        : Stream.empty());
        val properties = streamProperties(MixedIn.EXCLUDED);
        val collections = streamCollections(MixedIn.EXCLUDED);

        return _Streams.concat(self, actions, actionParameters, properties, collections);

    }

    /**
     * Introspecting up to the level required.
     * @since 2.0
     */
    void introspectUpTo(IntrospectionState upTo);

    /**
     * @return whether the corresponding type can be mapped onto a REFERENCE (schema) or an Oid,
     * that is the type is 'identifiable' (aka 'referencable' or 'bookmarkable')
     * @since 2.0
     */
    default boolean isIdentifiable() {
        return isManagedBean() || isViewModel() || isEntity();
    }

    /**
     * Delegates to {@link ObjectManager#createObject(org.apache.isis.core.metamodel.objectmanager.create.ObjectCreator.Request)}
     * @since 2.0
     */
    default ManagedObject createObject() {
        val mmc = getMetaModelContext();
        val objectCreateRequest = ObjectCreator.Request.of(this);
        val managedObject = mmc.getObjectManager().createObject(objectCreateRequest);
        return managedObject;
    }

    // -- TYPE COMPATIBILITY UTILITIES

    default public void assertPojoCompatible(final @Nullable Object pojo) {

        // can do this check only when the pojo is not null, otherwise is always considered valid
        if(pojo==null) {
            return;
        }

        if(!isPojoCompatible(pojo)) {
            val expectedType = getCorrespondingClass();
            throw _Exceptions.illegalArgument(
                    "Pojo not compatible with ObjectSpecification, " +
                    "objectSpec.correspondingClass = %s, " +
                    "pojo.getClass() = %s, " +
                    "pojo.toString() = %s",
                    expectedType, pojo.getClass(), pojo.toString());
        }
    }

    default public boolean isPojoCompatible(final Object pojo) {

        if(pojo==null) {
            return true;
        }

        val expectedType = getCorrespondingClass();
        val actualType = pojo.getClass();

        if(expectedType.isAssignableFrom(actualType)
                || ClassExtensions.equalsWhenBoxing(expectedType, actualType)) {
            return true;
        }

        val elementSpec = getElementSpecification()
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
        return  getCorrespondingClass().getName();
    }

    default Stream<ObjectSpecification> streamTypeHierarchy() {
        return superclass()!=null
                ? Stream.concat(Stream.of(this), superclass().streamTypeHierarchy())
                : Stream.of(this);
    }

    // -- VALUE SEMANTICS SUPPORT

    @SuppressWarnings("unchecked")
    default Optional<Parser<?>> selectParser(final ObjectFeature objFeature) {
        return lookupFacet(ValueFacet.class)
                .flatMap(valueFacet->valueFacet.selectParserForFeature(objFeature));
    }

    default Parser<?> selectParserElseFallback(final ObjectFeature objFeature) {
        return lookupFacet(ValueFacet.class)
                .map(valueFacet->
                        valueFacet.selectParserForFeatureElseFallback(objFeature))
                .orElseGet(()->ValueFacetAbstract.fallbackParser(
                        getLogicalType(),
                        objFeature.getFeatureIdentifier()));
    }



}
