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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.id.ObjectSpecId;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Objects;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.commons.MethodExtensions;
import org.apache.isis.core.metamodel.commons.MethodUtil;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.facets.object.entity.PersistenceStandard;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectVisibilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * A collection of utilities for {@link ManagedObject}. 
 * @since 2.0
 *
 */
@UtilityClass
@Log4j2
public final class ManagedObjects {
    
    // -- CATEGORISATION

    /** is null or has neither an ObjectSpecification and a value (pojo) */
    public static boolean isNullOrUnspecifiedOrEmpty(@Nullable ManagedObject adapter) {
        if(adapter==null || adapter==ManagedObject.unspecified()) {
            return true;
        }
        return adapter.getPojo()==null;
    }
    
    /** whether has at least a spec */
    public static boolean isSpecified(@Nullable ManagedObject adapter) {
        return adapter!=null && adapter!=ManagedObject.unspecified();
    }
    
    /**
     * @return whether the corresponding type can be mapped onto a REFERENCE (schema) or an Oid,
     * that is, the type is 'identifiable' (aka 'referencable' or 'bookmarkable') 
     */
    public static boolean isIdentifiable(@Nullable ManagedObject managedObject) {
        return spec(managedObject)
                .map(ObjectSpecification::isIdentifiable)
                .orElse(false);
    }
    
    public static boolean isEntity(ManagedObject managedObject) {
        return spec(managedObject)
                .map(ObjectSpecification::isEntity)
                .orElse(false);
    }

    public static boolean isValue(ManagedObject managedObject) {
        return spec(managedObject)
                .map(ObjectSpecification::isValue)
                .orElse(false);
    }
    
    public static Optional<String> getDomainType(ManagedObject managedObject) {
        return spec(managedObject)
                .map(ObjectSpecification::getSpecId)
                .map(ObjectSpecId::asString);
    }
    
    // -- IDENTIFICATION
    
    public static Optional<ObjectSpecification> spec(@Nullable ManagedObject managedObject) {
        return isSpecified(managedObject) ? Optional.of(managedObject.getSpecification()) : Optional.empty(); 
    }
    
    public static Optional<RootOid> identify(@Nullable ManagedObject managedObject) {
        return isSpecified(managedObject) ? managedObject.getRootOid() : Optional.empty(); 
    }
    
    public static RootOid identifyElseFail(@Nullable ManagedObject managedObject) {
        return identify(managedObject)
                .orElseThrow(()->_Exceptions.illegalArgument("cannot identify %s", managedObject));
    }
    
    public static Optional<Bookmark> bookmark(@Nullable ManagedObject managedObject) {
        return identify(managedObject)
                .map(RootOid::asBookmark);
    }
    
    public static Bookmark bookmarkElseFail(@Nullable ManagedObject managedObject) {
        return bookmark(managedObject)
                .orElseThrow(()->_Exceptions.illegalArgument("cannot bookmark %s", managedObject));
    }
    
    /**
     * @param managedObject
     * @return optionally a String representing a reference to the <em>identifiable</em> 
     * {@code managedObject}, usually made up of the object's type and its ID.
     */
    public static Optional<String> stringify(@Nullable ManagedObject managedObject) {
        return identify(managedObject)
                .map(RootOid::enString);
    }
    
    public static String stringifyElseFail(@Nullable ManagedObject managedObject) {
        return stringify(managedObject)
                .orElseThrow(()->_Exceptions.illegalArgument("cannot stringify %s", managedObject));
    }
    
    /**
     * 
     * @param managedObject
     * @param separator custom separator
     * @return optionally a String representing a reference to the <em>identifiable</em> 
     * {@code managedObject}, made of the form &lt;object-type&gt; &lt;separator&gt; &lt;object-id&gt;.
     */
    public static Optional<String> stringify(
            @Nullable ManagedObject managedObject, 
            @NonNull final String separator) {
        return identify(managedObject)
                .map(rootOid->rootOid.getLogicalTypeName() + separator + rootOid.getIdentifier());
    }

    public static String stringifyElseFail(
            @Nullable ManagedObject managedObject, 
            @NonNull final String separator) {
        return stringify(managedObject, separator)
                .orElseThrow(()->_Exceptions.illegalArgument("cannot stringify %s", managedObject));
    }


    // -- COMPARE UTILITIES

    public static int compare(@Nullable ManagedObject p, @Nullable ManagedObject q) {
        return NATURAL_NULL_FIRST.compare(p, q);
    }

    public static Comparator<ManagedObject> orderingBy(ObjectAssociation sortProperty, boolean ascending) {

        final Comparator<ManagedObject> comparator = ascending 
                ? NATURAL_NULL_FIRST 
                : NATURAL_NULL_FIRST.reversed();

        return (p, q) -> {
            val pSort = sortProperty.get(p, InteractionInitiatedBy.FRAMEWORK);
            val qSort = sortProperty.get(q, InteractionInitiatedBy.FRAMEWORK);
            return comparator.compare(pSort, qSort);
        };

    }

    // -- PREDEFINED COMPARATOR

    private static final Comparator<ManagedObject> NATURAL_NULL_FIRST = new Comparator<ManagedObject>(){
        @SuppressWarnings({"rawtypes" })
        @Override
        public int compare(@Nullable ManagedObject p, @Nullable ManagedObject q) {
            val pPojo = UnwrapUtil.single(p);
            val qPojo = UnwrapUtil.single(q);
            if(pPojo instanceof Comparable && qPojo instanceof Comparable) {
                return _Objects.compareNullsFirst((Comparable)pPojo, (Comparable)qPojo);
            }
            if(Objects.equals(pPojo, qPojo)) {
                return 0;
            }

            final int hashCompare = Integer.compare(Objects.hashCode(pPojo), Objects.hashCode(qPojo));
            if(hashCompare!=0) {
                return hashCompare;
            }
            //XXX what to return on hash-collision?
            return -1;
        }

    };
    
    // -- COPY UTILITIES
    
    @Nullable 
    public static ManagedObject copyIfClonable(@Nullable ManagedObject adapter) {

        if(adapter==null) {
            return null;
        }
        
        val viewModelFacet = adapter.getSpecification().getFacet(ViewModelFacet.class);
        if(viewModelFacet != null) {
            val viewModelPojo = adapter.getPojo();
            if(viewModelFacet.isCloneable(viewModelPojo)) {
                return ManagedObject.of(
                        adapter.getSpecification(), 
                        viewModelFacet.clone(viewModelPojo));
            }
        }
        
        return adapter;
        
    }
    
    // -- DEFAULTS UTILITIES
    
    public static ManagedObject emptyToDefault(boolean mandatory, @NonNull ManagedObject input) {
        if(!isSpecified(input)) {
            return input;
        }
        if(input.getPojo()!=null) {
            return input;
        }
        
        // there are 2 cases to handle here
        // 1) if primitive, then don't return null
        // 2) if boxed boolean, that is MANDATORY, then don't return null
        
        val spec = input.getSpecification();
        val expectedType = spec.getCorrespondingClass();
        if(expectedType.isPrimitive()) {
            return ManagedObject.of(spec, ClassExtensions.toDefault(expectedType));
        }
        if(Boolean.class.equals(expectedType) && mandatory) {
            return ManagedObject.of(spec, Boolean.FALSE);
        }
        
        return input;
    }
    
    // -- TITLE UTILITIES
    
    public static String abbreviatedTitleOf(ManagedObject adapter, int maxLength, String suffix) {
        return abbreviated(titleOf(adapter), maxLength, suffix);
    }
    
    public static String titleOf(ManagedObject adapter) {
        return adapter!=null?adapter.titleString(null):"";
    }

    private static String abbreviated(final String str, final int maxLength, String suffix) {
        return str.length() < maxLength ? str : str.substring(0, maxLength - 3) + suffix;
    }
    
    // -- COMMON SUPER TYPE FINDER
    
    /**
     * Find an ObjectSpecification that is common to all provided {@code objects} 
     * @param objects
     * @return optionally the common ObjectSpecification based on whether provided {@code objects}
     * are not empty
     */
    public static Optional<ObjectSpecification> commonSpecification(
            @Nullable final Can<ManagedObject> objects) {
        
        if (_NullSafe.isEmpty(objects)) {
            return Optional.empty();
        }
        val firstElement = objects.getFirstOrFail();
        val firstElementSpec = firstElement.getSpecification();
        
        if(objects.getCardinality().isOne()) {
            return Optional.of(firstElementSpec);
        }

        val commonSuperClassFinder = new ClassExtensions.CommonSuperclassFinder();
        objects.stream()
        .map(ManagedObject::getPojo)
        .filter(_NullSafe::isPresent)
        .forEach(commonSuperClassFinder::collect);
        
        val commonSuperClass = commonSuperClassFinder.getCommonSuperclass().orElse(null);
        if(commonSuperClass!=null && commonSuperClass!=firstElement.getSpecification().getCorrespondingClass()) {
            val specificationLoader = firstElementSpec.getMetaModelContext().getSpecificationLoader();
            val commonSpec = specificationLoader.loadSpecification(commonSuperClass);
            return Optional.of(commonSpec);
        }
        
        return Optional.of(firstElementSpec);
    }
    
    // -- ADABT UTILITIES
    
    public static Can<ManagedObject> adaptMultipleOfType(
            @NonNull  final ObjectSpecification elementSpec,
            @Nullable final Object collectionOrArray) {
        
        return _NullSafe.streamAutodetect(collectionOrArray)
        .map(pojo->ManagedObject.of(elementSpec, pojo)) // pojo is nullable here
        .collect(Can.toCan());
    }

    /**
     * used eg. to adapt the result of supporting methods, that return choice pojos
     */
    public static Can<ManagedObject> adaptMultipleOfTypeThenAttachThenFilterByVisibility(
            @NonNull  final ObjectSpecification elementSpec,
            @Nullable final Object collectionOrArray, 
            @NonNull  final InteractionInitiatedBy interactionInitiatedBy) {
        
        return _NullSafe.streamAutodetect(collectionOrArray)
        .map(pojo->ManagedObject.of(elementSpec, pojo)) // pojo is nullable here
        .map(ManagedObjects.EntityUtil::reattach)
        .filter(ManagedObjects.VisibilityUtil.filterOn(interactionInitiatedBy))
        .collect(Can.toCan());
    }

    /**
     * TODO just for debugging, remove!
     * print stacktrace to console, if {@code pojo} is an attached entity
     * @deprecated
     */
    @SneakyThrows
    public static void warnIfAttachedEntity(ManagedObject adapter, String logMessage) {
        if(isNullOrUnspecifiedOrEmpty(adapter)) {
            return;
        }
        if(EntityUtil.isAttached(adapter)) {
            _Probe.errOut("%s [%s]", logMessage, adapter.getSpecification().getFullIdentifier());
            _Exceptions.dumpStackTrace();    
        }
    }
    
    /**
     * eg. in order to prevent wrapping an object that is already wrapped
     */
    public static void assertPojoNotManaged(@Nullable Object pojo) {
        // can do this check only when the pojo is not null, otherwise is always considered valid
        if(pojo==null) {
            return;
        }
        
        if(pojo instanceof ManagedObject) {
            throw _Exceptions.illegalArgument(
                    "Cannot adapt a pojo of type ManagedObject, " +
                    "pojo.getClass() = %s, " +
                    "pojo.toString() = %s",
                    pojo.getClass(), pojo.toString());
        }
    }


    // -- ENTITY UTILITIES
    
    @UtilityClass
    public static final class EntityUtil {
        
        @NonNull
        public static Optional<PersistenceStandard> getPersistenceStandard(@Nullable ManagedObject adapter) {
            if(adapter==null) {
                return Optional.empty();
            }
            val spec = adapter.getSpecification();
            if(spec==null || !spec.isEntity()) {
                return Optional.empty();
            }

            val entityFacet = spec.getFacet(EntityFacet.class);
            if(entityFacet==null) {
                return Optional.empty();
            }
            
            return Optional.of(entityFacet.getPersistenceStandard());
        }
        
        @NonNull
        public static EntityState getEntityState(@Nullable ManagedObject adapter) {
            if(adapter==null) {
                return EntityState.NOT_PERSISTABLE;
            }
            val spec = adapter.getSpecification();
            val pojo = adapter.getPojo();
            
            if(spec==null || pojo==null || !spec.isEntity()) {
                return EntityState.NOT_PERSISTABLE;
            }

            val entityFacet = spec.getFacet(EntityFacet.class);
            if(entityFacet==null) {
                throw _Exceptions.unrecoverable("Entity types must have an EntityFacet");
            }

            return entityFacet.getEntityState(pojo);
        }
        
        public static void persistInCurrentTransaction(ManagedObject managedObject) {
            requiresEntity(managedObject);
            val spec = managedObject.getSpecification();
            val entityFacet = spec.getFacet(EntityFacet.class);
            entityFacet.persist(spec, managedObject.getPojo());
        }
        
        public static void destroyInCurrentTransaction(ManagedObject managedObject) {
            requiresEntity(managedObject);
            val spec = managedObject.getSpecification();
            val entityFacet = spec.getFacet(EntityFacet.class);
            entityFacet.delete(spec, managedObject.getPojo());
        }
        
        public static void requiresEntity(ManagedObject managedObject) {
            if(isNullOrUnspecifiedOrEmpty(managedObject)) {
                throw _Exceptions.illegalArgument("requires an entity object but got null, unspecified or empty");
            }
            val spec = managedObject.getSpecification();
            if(!spec.isEntity()) {
                throw _Exceptions.illegalArgument("not an entity type %s (sort=%s)",
                        spec.getCorrespondingClass(), 
                        spec.getBeanSort());                
            }
        }
        
        /**
         * @param managedObject
         * @return managedObject
         * @throws AssertionError if managedObject is a detached entity  
         */
        @NonNull
        public static ManagedObject requiresAttached(@NonNull ManagedObject managedObject) {
            val entityState = EntityUtil.getEntityState(managedObject);
            if(entityState.isPersistable()) {
                // ensure we have an attached entity
                _Assert.assertEquals(
                        EntityState.PERSISTABLE_ATTACHED, 
                        entityState,
                        ()-> String.format("entity %s is required to be attached (not detached)", 
                                managedObject.getSpecification().getSpecId()));
            }
            return managedObject;
        }
        
        @Nullable
        public static ManagedObject reattach(@Nullable ManagedObject managedObject) {
            if(isNullOrUnspecifiedOrEmpty(managedObject)) {
                return managedObject;
            }
            val entityState = EntityUtil.getEntityState(managedObject);
            if(!entityState.isPersistable()) {
                return managedObject;
            }
            if(!entityState.isDetached()) {
                return managedObject;
            }
            
            // identification (on JDO) fails, when detached object, where rootOid was not previously memoized
            if(EntityUtil.getPersistenceStandard(managedObject)
                        .map(PersistenceStandard::isJdo)
                        .orElse(false)
                    && !managedObject.isRootOidMemoized()) {
                val msg = String.format("entity %s is required to have a memoized ID, "
                        + "otherwise cannot re-attach", 
                        managedObject.getSpecification().getSpecId());
                log.error(msg); // in case exception gets swallowed
                throw _Exceptions.illegalState(msg);
            }
            
            val objectIdentifier = identify(managedObject)
                    .map(RootOid::getIdentifier);
                    
            if(!objectIdentifier.isPresent()) {
                return managedObject;
            }
            
            val objectLoadRequest = ObjectLoader.Request.of(
                    managedObject.getSpecification(), 
                    objectIdentifier.get());
            
            return managedObject.getObjectManager().loadObject(objectLoadRequest);
        }
        
        public static void requiresWhenFirstIsBookmarkableSecondIsAttached(
                ManagedObject first,
                ManagedObject second) {

            if(!ManagedObjects.isIdentifiable(first) || !ManagedObjects.isSpecified(second)) {
                return;
            }
            val secondSpec = second.getSpecification();
            if(secondSpec.isParented() || !secondSpec.isEntity()) {
                return;
            }

            if(!EntityUtil.isAttached(second)) {
                throw _Exceptions.illegalArgument(
                        "can't set a reference to a transient object [%s] from a persistent one [%s]",
                        second,
                        first.titleString(null));
            }
        }
        
        // -- SHORTCUTS
        
        public static boolean isAttached(@Nullable ManagedObject adapter) {
            return EntityUtil.getEntityState(adapter).isAttached();
        }
        
        public static boolean isDetached(@Nullable ManagedObject adapter) {
            return EntityUtil.getEntityState(adapter).isDetached();
        }
        
        public static boolean isDestroyed(@Nullable ManagedObject adapter) {
            return EntityUtil.getEntityState(adapter).isDestroyed();
        }
        
    }

    // -- VISIBILITY UTIL
    
    @UtilityClass
    public static final class VisibilityUtil {
    
        public static Predicate<? super ManagedObject> filterOn(InteractionInitiatedBy interactionInitiatedBy) {
            return $->ManagedObjects.VisibilityUtil.isVisible($, interactionInitiatedBy);
        }
    
        /**
         * Filters a collection (an adapter around either a Collection or an Object[]) and returns a stream of
         * {@link ManagedObject}s of those that are visible (as per any facet(s) installed on the element class
         * of the collection).
         * @param collectionAdapter - an adapter around a collection (as returned by a getter of a collection, or of an autoCompleteNXxx() or choicesNXxx() method, etc
         * @param interactionInitiatedBy
         */
        public static Stream<ManagedObject> streamVisibleAdapters(
                final ManagedObject collectionAdapter,
                final InteractionInitiatedBy interactionInitiatedBy) {
    
            return CollectionFacet.streamAdapters(collectionAdapter)
                    .filter(VisibilityUtil.filterOn(interactionInitiatedBy));
        }
        
        private static Stream<Object> streamVisiblePojos(
                final ManagedObject collectionAdapter,
                final InteractionInitiatedBy interactionInitiatedBy) {
    
            return CollectionFacet.streamAdapters(collectionAdapter)
                    .filter(VisibilityUtil.filterOn(interactionInitiatedBy))
                    .map(UnwrapUtil::single);
        }
        
        public static Object[] visiblePojosAsArray(
                final ManagedObject collectionAdapter,
                final InteractionInitiatedBy interactionInitiatedBy) {
    
            return streamVisiblePojos(collectionAdapter, interactionInitiatedBy)
                    .collect(_Arrays.toArray(Object.class));
        }
        
        public static Object visiblePojosAutofit(
                final ManagedObject collectionAdapter,
                final InteractionInitiatedBy interactionInitiatedBy,
                final Class<?> requiredContainerType) {
            
            val visiblePojoStream = streamVisiblePojos(collectionAdapter, interactionInitiatedBy);
            val autofittedObjectContainer = CollectionFacet.AutofitUtils
                    .collect(visiblePojoStream, requiredContainerType);
            return autofittedObjectContainer;
        }
        
        
        /**
         * @param adapter - an adapter around the domain object whose visibility is being checked
         * @param interactionInitiatedBy
         */
        public static boolean isVisible(
                ManagedObject adapter,
                InteractionInitiatedBy interactionInitiatedBy) {
    
            if(isNullOrUnspecifiedOrEmpty(adapter)) {
                // a choices list could include a null (eg example in ToDoItems#choices1Categorized()); want to show as "visible"
                return true;
            }
            val spec = adapter.getSpecification();
            if(spec.isEntity()) {
                if(EntityUtil.isDestroyed(adapter)) {
                    return false;
                }
            }
            if(interactionInitiatedBy == InteractionInitiatedBy.FRAMEWORK) { 
                return true; 
            }
            val visibilityContext = createVisibleInteractionContext(
                    adapter,
                    InteractionInitiatedBy.USER,
                    Where.OBJECT_FORMS);
    
            return InteractionUtils.isVisibleResult(spec, visibilityContext)
                    .isNotVetoing();
        }
        
        private static VisibilityContext createVisibleInteractionContext(
                final ManagedObject adapter,
                final InteractionInitiatedBy interactionInitiatedBy,
                final Where where) {
            
            return new ObjectVisibilityContext(
                    adapter,
                    adapter.getSpecification().getIdentifier(),
                    interactionInitiatedBy,
                    where);
        }
    
    }
    
    
    // -- INVOCATION UTILITY
    
    @UtilityClass
    public static final class InvokeUtil {
    
        public static Object invokeWithPPM(
                final Constructor<?> ppmConstructor, 
                final Method method, 
                final ManagedObject adapter, 
                final Can<ManagedObject> pendingArguments,
                final List<Object> additionalArguments) {
            
            val ppmTuple = MethodExtensions.construct(ppmConstructor, UnwrapUtil.multipleAsArray(pendingArguments));
            val paramPojos = _Arrays.combineWithExplicitType(Object.class, ppmTuple, additionalArguments.toArray());
            return MethodExtensions.invoke(method, UnwrapUtil.single(adapter), paramPojos);
        }
        
        public static Object invokeWithPPM(
                final Constructor<?> ppmConstructor, 
                final Method method, 
                final ManagedObject adapter, 
                final Can<ManagedObject> argumentAdapters) {
            return invokeWithPPM(ppmConstructor, method, adapter, argumentAdapters, Collections.emptyList());
        }
        
        public static void invokeAll(Collection<Method> methods, final ManagedObject adapter) {
            MethodUtil.invoke(methods, UnwrapUtil.single(adapter));
        }
    
        public static Object invoke(Method method, ManagedObject adapter) {
            return MethodExtensions.invoke(method, UnwrapUtil.single(adapter));
        }
    
        public static Object invoke(Method method, ManagedObject adapter, Object arg0) {
            return MethodExtensions.invoke(method, UnwrapUtil.single(adapter), new Object[] {arg0});
        }
    
        public static Object invoke(Method method, ManagedObject adapter, Can<ManagedObject> argumentAdapters) {
            return MethodExtensions.invoke(method, UnwrapUtil.single(adapter), UnwrapUtil.multipleAsArray(argumentAdapters));
        }
    
        public static Object invoke(Method method, ManagedObject adapter, ManagedObject arg0Adapter) {
            return invoke(method, adapter, UnwrapUtil.single(arg0Adapter));
        }
    
        public static Object invoke(Method method, ManagedObject adapter, ManagedObject[] argumentAdapters) {
            return MethodExtensions.invoke(method, UnwrapUtil.single(adapter), UnwrapUtil.multipleAsArray(argumentAdapters));
        }

        /**
         * Invokes the method, adjusting arguments as required to make them fit the method's parameters.
         * <p>
         * That is:
         * <ul>
         * <li>if the method declares parameters but arguments are missing, then will provide 'null' defaults for these.</li>
         * </ul>
         */
        public static Object invokeAutofit(Method method, ManagedObject adapter) {
            return invoke(method, adapter, new ManagedObject[method.getParameterTypes().length]);
        }
    
        /**
         * Invokes the method, adjusting arguments as required to make them fit the method's parameters.
         * <p>
         * That is:
         * <ul>
         * <li>if the method declares parameters but arguments are missing, then will provide 'null' defaults for these.</li>
         * <li>if the method does not declare all parameters for arguments, then truncates arguments.</li>
         * <li>any {@code additionalArgValues} must also fit at the end of the resulting parameter list</li>
         * </ul>
         */
        public static Object invokeAutofit(
                final Method method, 
                final ManagedObject target, 
                final Can<? extends ManagedObject> pendingArgs,
                final List<Object> additionalArgValues) {
    
            val argArray = adjust(method, pendingArgs, additionalArgValues);
            
            return MethodExtensions.invoke(method, UnwrapUtil.single(target), argArray);
        }
    
        /**
         * same as {@link #invokeAutofit(Method, ManagedObject, Can, List)} w/o additionalArgValues
         */
        public static Object invokeAutofit(
                final Method method, 
                final ManagedObject target, 
                final Can<? extends ManagedObject> pendingArgs) {
            
            return invokeAutofit(method, target, pendingArgs, Collections.emptyList());
        }
    
        private static Object[] adjust(
                final Method method, 
                final Can<? extends ManagedObject> pendingArgs,
                final List<Object> additionalArgValues) {
            
            val parameterTypes = method.getParameterTypes();
            val paramCount = parameterTypes.length;
            val additionalArgCount = additionalArgValues.size();
            val pendingArgsToConsiderCount = paramCount - additionalArgCount;
            
            val argIterator = argIteratorFrom(pendingArgs);
            val adjusted = new Object[paramCount];
            for(int i=0; i<pendingArgsToConsiderCount; i++) {
                
                val paramType = parameterTypes[i];
                val arg = argIterator.hasNext() ? UnwrapUtil.single(argIterator.next()) : null;
                
                adjusted[i] = honorPrimitiveDefaults(paramType, arg);
            }
            
            // add the additional parameter values (if any)
            int paramIndex = pendingArgsToConsiderCount;
            for(val additionalArg : additionalArgValues) {
                val paramType = parameterTypes[paramIndex];
                adjusted[paramIndex] = honorPrimitiveDefaults(paramType, additionalArg);
                ++paramIndex;
            }
            
            return adjusted;
    
        }
    
        private static Iterator<? extends ManagedObject> argIteratorFrom(Can<? extends ManagedObject> pendingArgs) {
            return pendingArgs!=null ? pendingArgs.iterator() : Collections.emptyIterator();
        }
    
        private static Object honorPrimitiveDefaults(
                final Class<?> expectedType, 
                final @Nullable Object value) {
            
            if(value == null && expectedType.isPrimitive()) {
                return ClassExtensions.toDefault(expectedType);
            }
            return value;
        }
        
    
    }
    
    // -- UNWRAP UTILITY
    
    @UtilityClass
    public static final class UnwrapUtil {
        
        // -- SINGLE
        
        @Nullable
        public static Object single(@Nullable final ManagedObject adapter) {
            return ManagedObjects.isSpecified(adapter)
                    ? adapter.getPojo() 
                    : null;
        }
        
        @Nullable
        public static String singleAsStringOrElse(@Nullable final ManagedObject adapter, @Nullable String orElse) {
            final Object obj = UnwrapUtil.single(adapter);
            if (obj == null) {
                return null;
            }
            if (obj instanceof String) {
                return (String) obj;
            }
            return orElse;
        }
        
        // -- AS ARRAY
        
        @Nullable
        public static Object[] multipleAsArray(@NonNull final Can<ManagedObject> adapters) {
            val unwrappedObjects = _Arrays.mapCollection(adapters.toList(), UnwrapUtil::single);
            return unwrappedObjects;
        }
        
        @Nullable
        public static Object[] multipleAsArray(@Nullable final Collection<ManagedObject> adapters) {
            val unwrappedObjects = _Arrays.mapCollection(adapters, UnwrapUtil::single);
            return unwrappedObjects;
        }

        @Nullable
        public static Object[] multipleAsArray(@Nullable final ManagedObject[] adapters) {
            val unwrappedObjects = _Arrays.map(adapters, UnwrapUtil::single);
            return unwrappedObjects;
        }
        
        // -- AS LIST

        /**
         * 
         * @param adapters
         * @return non-null, unmodifiable
         */
        public static List<Object> multipleAsList(@Nullable final Collection<? extends ManagedObject> adapters) {
            if (adapters == null) {
                return Collections.emptyList();
            }
            return adapters.stream()
                    .map(UnwrapUtil::single)
                    .collect(_Lists.toUnmodifiable());
        }
        
        /**
         * 
         * @param adapters
         * @return non-null, unmodifiable
         */
        public static List<Object> multipleAsList(@Nullable final Can<? extends ManagedObject> adapters) {
            if (adapters == null) {
                return Collections.emptyList();
            }
            return adapters.stream()
                    .map(UnwrapUtil::single)
                    .collect(_Lists.toUnmodifiable());
        }


        /**
         * 
         * @param adapters
         * @return non-null, unmodifiable
         */
        public static Set<Object> multipleAsSet(@Nullable final Collection<? extends ManagedObject> adapters) {
            if (adapters == null) {
                return Collections.emptySet();
            }
            return adapters.stream()
                    .map(UnwrapUtil::single)
                    .collect(_Sets.toUnmodifiable());
        }
        
    }
    

}
