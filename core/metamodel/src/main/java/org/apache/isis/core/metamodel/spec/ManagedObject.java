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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.domain.DomainObjectList;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.base._Tuples.Indexed;
import org.apache.isis.core.commons.internal.collections._Arrays;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.commons.MethodExtensions;
import org.apache.isis.core.metamodel.commons.MethodUtil;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectVisibilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.objectmanager.create.ObjectCreator;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderDefault;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.val;

/**
 * Represents an instance of some element of the meta-model managed by the framework, 
 * that is IoC-container provided beans, persistence-stack provided entities or view-models.  
 *
 */
public interface ManagedObject {

    /**
     * Returns the specification that details the structure (meta-model) of this object.
     */
    ObjectSpecification getSpecification();

    /**
     * Returns the adapted domain object, the 'plain old java' object this managed object 
     * represents with the framework.
     */
    Object getPojo();

    // -- SIMPLE

    @Value @RequiredArgsConstructor(staticName="of") @EqualsAndHashCode(of = "pojo") 
    static final class SimpleManagedObject implements ManagedObject {
        @NonNull private final ObjectSpecification specification;
        @NonNull private final Object pojo;
    }

    // -- LAZY

    @ToString(of = {"specification", "pojo"}) @EqualsAndHashCode(of = "pojo")
    static final class LazyManagedObject implements ManagedObject {

        @NonNull private final Function<Class<?>, ObjectSpecification> specLoader;  

        @Getter @NonNull private final Object pojo;

        @Getter(lazy=true) 
        private final ObjectSpecification specification = specLoader.apply(pojo.getClass());

        public LazyManagedObject(@NonNull Function<Class<?>, ObjectSpecification> specLoader, @NonNull Object pojo) {
            this.specLoader = specLoader;
            this.pojo = pojo;
        }

    }

    // -- TITLE

    public default String titleString() {
        return titleString(null);
    }

    default String titleString(@Nullable ManagedObject contextAdapterIfAny) {
        return TitleUtil.titleString(this, contextAdapterIfAny);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class TitleUtil {

        private static String titleString(ManagedObject managedObject, ManagedObject contextAdapterIfAny) {
            if (managedObject.getSpecification().isParentedOrFreeCollection()) {
                final CollectionFacet facet = managedObject.getSpecification().getFacet(CollectionFacet.class);
                return collectionTitleString(managedObject, facet);
            } else {
                return objectTitleString(managedObject, contextAdapterIfAny);
            }
        }

        private static String objectTitleString(ManagedObject managedObject, ManagedObject contextAdapterIfAny) {
            if (managedObject.getPojo() instanceof String) {
                return (String) managedObject.getPojo();
            }
            final ObjectSpecification specification = managedObject.getSpecification();
            String title = specification.getTitle(contextAdapterIfAny, managedObject);

            if (title == null) {
                title = getDefaultTitle(managedObject);
            }
            return title;
        }

        private static String collectionTitleString(ManagedObject managedObject, final CollectionFacet facet) {
            final int size = facet.size(managedObject);
            final ObjectSpecification elementSpecification = managedObject.getElementSpecification();
            if (elementSpecification == null || elementSpecification.getFullIdentifier().equals(Object.class.getName())) {
                switch (size) {
                case -1:
                    return "Objects";
                case 0:
                    return "No objects";
                case 1:
                    return "1 object";
                default:
                    return size + " objects";
                }
            } else {
                switch (size) {
                case -1:
                    return elementSpecification.getPluralName();
                case 0:
                    return "No " + elementSpecification.getPluralName();
                case 1:
                    return "1 " + elementSpecification.getSingularName();
                default:
                    return size + " " + elementSpecification.getPluralName();
                }
            }
        }

        private static String getDefaultTitle(ManagedObject managedObject) {
            return "A" + (" " + managedObject.getSpecification().getSingularName()).toLowerCase();
        }

    }

    // -- SHORTCUT - ELEMENT SPECIFICATION

    /**
     * Used only for (standalone or parented) collections.
     * @deprecated use {@link ObjectSpecification#getElementSpecification()} instead, 
     * (proposed for removal, to keep the API slim)
     */
    @Deprecated
    default public ObjectSpecification getElementSpecification() {
        return getSpecification().getElementSpecification();
    }

    // -- SHORTCUT - ICON NAME

    /**
     * Returns the name of an icon to use if this object is to be displayed
     * graphically.
     * <p>
     * May return <code>null</code> if no icon is specified.
     * @deprecated use {@link ObjectSpecification#getIconName(ManagedObject))} instead, 
     * (proposed for removal, to keep the API slim)
     */
    default public String getIconName() {
        return getSpecification().getIconName(this);
    }

    // -- FACTORIES

    /**
     * Optimized for cases, when the pojo's specification is already available.
     * @param specification
     * @param pojo
     * @return
     */
    public static ManagedObject of(ObjectSpecification specification, Object pojo) {
        if(!specification.getCorrespondingClass().isAssignableFrom(pojo.getClass())) {
            throw _Exceptions.illegalArgument(
                    "Pojo not compatible with ObjectSpecification, " +
                    "objectSpec.correspondingClass = %s, " +
                    "pojo.getClass() = %s, " +
                    "pojo.toString() = %s",
                    specification.getCorrespondingClass(), pojo.getClass(), pojo.toString());
        }
        return new SimpleManagedObject(specification, pojo);
    }

    /**
     * For cases, when the pojo's specification is not available and needs to be looked up. 
     * @param specLoader
     * @param pojo
     * @return
     */
    public static ManagedObject of(
            Function<Class<?>, ObjectSpecification> specLoader, 
            Object pojo) {

        return new LazyManagedObject(specLoader, pojo);
    }

    // -- UNWRAPPING

    @Nullable
    public static Object unwrapSingle(@Nullable final ManagedObject adapter) {
        return adapter != null ? adapter.getPojo() : null;
    }
    
    @Nullable
    public static Object[] unwrapMultipleAsArray(@Nullable final Collection<ManagedObject> adapters) {
        val unwrappedObjects = _Arrays.mapCollection(adapters, ManagedObject::unwrapSingle);
        return unwrappedObjects;
    }

    @Nullable
    public static Object[] unwrapMultipleAsArray(@Nullable final ManagedObject[] adapters) {
        val unwrappedObjects = _Arrays.map(adapters, ManagedObject::unwrapSingle);
        return unwrappedObjects;
    }

    @Nullable
    public static String unwrapSingleAsStringOrElse(@Nullable final ManagedObject adapter, @Nullable String orElse) {
        final Object obj = ManagedObject.unwrapSingle(adapter);
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof String)) {
            return orElse;
        }
        return (String) obj;
    }

    /**
     * 
     * @param adapters
     * @return non-null, unmodifiable
     */
    public static List<Object> unwrapMultipleAsList(@Nullable final Collection<? extends ManagedObject> adapters) {
        if (adapters == null) {
            return Collections.emptyList();
        }
        return adapters.stream()
                .map(ManagedObject::unwrapSingle)
                .collect(_Lists.toUnmodifiable());
    }
    
    /**
     * 
     * @param adapters
     * @return non-null, unmodifiable
     */
    public static List<Object> unwrapMultipleAsList(@Nullable final Can<? extends ManagedObject> adapters) {
        if (adapters == null) {
            return Collections.emptyList();
        }
        return adapters.stream()
                .map(ManagedObject::unwrapSingle)
                .collect(_Lists.toUnmodifiable());
    }


    /**
     * 
     * @param adapters
     * @return non-null, unmodifiable
     */
    public static Set<Object> unwrapMultipleAsSet(@Nullable final Collection<? extends ManagedObject> adapters) {
        if (adapters == null) {
            return Collections.emptySet();
        }
        return adapters.stream()
                .map(ManagedObject::unwrapSingle)
                .collect(_Sets.toUnmodifiable());
    }

    // -- SHORTCUTS

    public static String getDomainType(ManagedObject objectAdapter) {
        if (objectAdapter == null) {
            return null;
        }
        return objectAdapter.getSpecification().getSpecId().asString();
    }

    // -- BASIC PREDICATES

    static boolean isEntity(ManagedObject adapter) {
        if(adapter==null) {
            return false;
        }
        return adapter.getSpecification().isEntity();
    }

    static boolean isValue(ManagedObject adapter) {
        if(adapter==null) {
            return false;
        }
        return adapter.getSpecification().isValue();
    }

    /**
     * @return whether the corresponding type can be mapped onto a REFERENCE (schema) or an Oid,
     * that is the type is 'identifiable' (aka 'referencable' or 'bookmarkable') 
     */
    static boolean isIdentifiable(ManagedObject adapter) {
        if(adapter==null) {
            return false;
        }
        val spec = adapter.getSpecification();
        return spec.isIdentifiable();
    }

    static boolean isNull(ManagedObject adapter) {
        if(adapter==null) {
            return true;
        }
        return adapter.getPojo()==null;
    }

    // -- COMPARE UTILITIES
    
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class CompareUtil {

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
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public int compare(@Nullable ManagedObject p, @Nullable ManagedObject q) {
                val pPojo = ManagedObject.unwrapSingle(p);
                val qPojo = ManagedObject.unwrapSingle(q);
                if(pPojo instanceof Comparable && qPojo instanceof Comparable) {
                    return _NullSafe.compareNullsFirst((Comparable)pPojo, (Comparable)qPojo);
                }
                if(Objects.equals(pPojo, qPojo)) {
                    return 0;
                }
                
                final int hashCompare = Integer.compare(Objects.hashCode(pPojo), Objects.hashCode(qPojo));
                if(hashCompare!=0) {
                    return hashCompare;
                }
                //TODO what to return on hash-collision?
                return -1;
            }
            
        };
        
    }
    
    // -- VISIBILITY UTILITIES

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class VisibilityUtil {

        public static Predicate<? super ManagedObject> filterOn(InteractionInitiatedBy interactionInitiatedBy) {
            return $->ManagedObject.VisibilityUtil.isVisible($, interactionInitiatedBy);
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
                    .map(ManagedObject::unwrapSingle);
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

            if(adapter == null) {
                // a choices list could include a null (eg example in ToDoItems#choices1Categorized()); want to show as "visible"
                return true;
            }
            val spec = adapter.getSpecification();
            if(spec.isEntity()) {
                if(ManagedObject._isDestroyed(adapter)) {
                    return false;
                }
            }
            if(interactionInitiatedBy == InteractionInitiatedBy.FRAMEWORK) { 
                return true; 
            }
            return isVisibleForUser(adapter);
        }

        private static boolean isVisibleForUser(ManagedObject adapter) {
            val visibilityContext = createVisibleInteractionContextForUser(adapter);
            val spec = adapter.getSpecification();
            return InteractionUtils.isVisibleResult(spec, visibilityContext)
                    .isNotVetoing();
        }

        private static VisibilityContext<?> createVisibleInteractionContextForUser(
                ManagedObject adapter) {

            return new ObjectVisibilityContext(
                    adapter,
                    adapter.getSpecification().getIdentifier(),
                    InteractionInitiatedBy.USER,
                    Where.OBJECT_FORMS);
        }
    }

    // -- INVOCATION UTILITY

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class InvokeUtil {
    
        public static void invokeAll(Collection<Method> methods, final ManagedObject adapter) {
            MethodUtil.invoke(methods, unwrapSingle(adapter));
        }
    
        public static Object invoke(Method method, ManagedObject adapter) {
            return MethodExtensions.invoke(method, unwrapSingle(adapter));
        }
    
        public static Object invoke(Method method, ManagedObject adapter, Object arg0) {
            return MethodExtensions.invoke(method, unwrapSingle(adapter), new Object[] {arg0});
        }
    
        public static Object invoke(Method method, ManagedObject adapter, List<ManagedObject> argumentAdapters) {
            return MethodExtensions.invoke(method, unwrapSingle(adapter), unwrapMultipleAsArray(argumentAdapters));
        }

        public static Object invoke(Method method, ManagedObject adapter, ManagedObject arg0Adapter) {
            return invoke(method, adapter, unwrapSingle(arg0Adapter));
        }
    
        public static Object invoke(Method method, ManagedObject adapter, ManagedObject[] argumentAdapters) {
            return MethodExtensions.invoke(method, unwrapSingle(adapter), unwrapMultipleAsArray(argumentAdapters));
        }
    
        public static Object invokeC(
                Method method, 
                ManagedObject adapter, 
                Stream<Indexed<? extends ManagedObject>> paramsAndIndexes) {
            return invoke(method, adapter, asArray(paramsAndIndexes, method.getParameterTypes().length));
        }
    
        private static ManagedObject[] asArray(
                Stream<Indexed<? extends ManagedObject>> paramsAndIndexes, 
                int length) {
            
            final ManagedObject[] args = new ManagedObject[length];
            paramsAndIndexes.forEach(entry->{
                final int paramNum = entry.getIndex();
                if(paramNum < length) {
                    args[paramNum] = entry.getValue();
                }
            });
            return args;
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
                final List<? extends ManagedObject> pendingArgs,
                final List<Object> additionalArgValues) {
    
            val argArray = adjust(method, pendingArgs, additionalArgValues);
            
            return MethodExtensions.invoke(method, unwrapSingle(target), argArray);
        }

        /**
         * same as {@link #invokeAutofit(Method, ManagedObject, List, List)} w/o additionalArgValues
         */
        public static Object invokeAutofit(
                final Method method, 
                final ManagedObject target, 
                final List<? extends ManagedObject> pendingArgs) {
            
            return invokeAutofit(method, target, pendingArgs, Collections.emptyList());
        }
    
        private static Object[] adjust(
                final Method method, 
                final List<? extends ManagedObject> pendingArgs,
                final List<Object> additionalArgValues) {
            
            val parameterTypes = method.getParameterTypes();
            val paramCount = parameterTypes.length;
            val additionalArgCount = additionalArgValues.size();
            val pendingArgsToConsiderCount = paramCount - additionalArgCount;
            
            val argIterator = argIteratorFrom(pendingArgs);
            val adjusted = new Object[paramCount];
            for(int i=0; i<pendingArgsToConsiderCount; i++) {
                
                val paramType = parameterTypes[i];
                val arg = argIterator.hasNext() ? unwrapSingle(argIterator.next()) : null;
                
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

        private static Iterator<? extends ManagedObject> argIteratorFrom(List<? extends ManagedObject> pendingArgs) {
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
    
    // -- DEPRECATIONS (REFACTORING)

    static MetaModelContext _mmc(ManagedObject adapter) {
        return adapter.getSpecification().getMetaModelContext();
    }

    static RootOid _identify(ManagedObject adapter) {
        return _mmc(adapter).getObjectManager().identifyObject(adapter); 
    }

    static RootOid _identifyElseThrow(ManagedObject adapter) {
        try {
            val rootOid = _identify(adapter);
            if(rootOid!=null){
                return rootOid; 
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "ManagedObject must represent an identifiable object: " + adapter, e);
        }
        throw new IllegalArgumentException("ManagedObject must represent an identifiable object: " + adapter);
    }
    
    static String _instanceId(ManagedObject adapter) {
        val identifier = ManagedObject._identifyElseThrow(adapter).getIdentifier();
        return identifier; 
    }
    
    static EntityState _entityState(ManagedObject adapter) {
        if(adapter==null) {
            return EntityState.not_Persistable;
        }
        return _entityState(adapter.getSpecification(), adapter.getPojo());
    }
    
    static EntityState _entityState(ObjectSpecification spec, Object pojo) {

        if(spec==null || pojo==null || !spec.isEntity()) {
            return EntityState.not_Persistable;
        }

        val entityFacet = spec.getFacet(EntityFacet.class);
        if(entityFacet==null) {
            throw _Exceptions.unrecoverable("Entity types must have an EntityFacet");
        }

        return entityFacet.getEntityState(pojo);
    }
    

    static boolean _isDestroyed(ManagedObject adapter) {
        return _entityState(adapter) == EntityState.persistable_Destroyed;
    }

    static void _whenFirstIsBookmarkable_ensureSecondIsAsWell(
            ManagedObject first,
            ManagedObject second) {

        if(ManagedObject.isIdentifiable(first) && second!=null) {

            val refSpec = second.getSpecification();

            if(refSpec.isParented() || !refSpec.isEntity()) {
                return;
            }

            val entityState = _entityState(second);
            if(entityState != EntityState.persistable_Attached) {
                throw _Exceptions.illegalArgument(
                        "can't set a reference to a transient object [%s] from a persistent one [%s]",
                        second,
                        first.titleString(null));
            }
            
        }

    }

    // move this to ObjectManager?
    static ManagedObject _adapterOfRootOid(SpecificationLoader specificationLoader, RootOid rootOid) {

        val mmc = ((SpecificationLoaderDefault)specificationLoader).getMetaModelContext();

        val spec = specificationLoader.loadSpecification(rootOid.getObjectSpecId());
        val objectId = rootOid.getIdentifier();

        val objectLoadRequest = ObjectLoader.Request.of(spec, objectId);
        val managedObject = mmc.getObjectManager().loadObject(objectLoadRequest);

        return managedObject;

    }

    static ManagedObject _newTransientInstance(ObjectSpecification spec) {

        if(spec == null) {
            return null;
        }

        val mmc = spec.getMetaModelContext();

        val objectCreateRequest = ObjectCreator.Request.of(spec);
        val managedObject = mmc.getObjectManager().createObject(objectCreateRequest);

        return managedObject;

    }

    @Deprecated
    static ManagedObject _adapterOfList(SpecificationLoader specificationLoader, DomainObjectList list) {

        if(list == null) {
            return null;
        }

        val spec = specificationLoader.loadSpecification(list.getClass());
        return ManagedObject.of(spec, list);

        // legacy of
        // resourceContext.adapterOfPojo(list);
    }

    static void _makePersistentInTransaction(ManagedObject adapter) {

        val spec = adapter.getSpecification();
        if(spec.isEntity()) {
            val entityFacet = spec.getFacet(EntityFacet.class);
            entityFacet.persist(spec, adapter.getPojo());
            return;
        }

        throw _Exceptions.illegalArgument("not an entity type %s (sort=%s)", spec.getCorrespondingClass(), spec.getBeanSort());
    }
    
    static void _destroyObjectInTransaction(ManagedObject adapter) {
        // legacy of
        //getPersistenceSession().destroyObjectInTransaction(adapter);
        
        val spec = adapter.getSpecification();
        if(spec.isEntity()) {
            val entityFacet = spec.getFacet(EntityFacet.class);
            entityFacet.delete(spec, adapter.getPojo());
            return;
        }

        throw _Exceptions.unexpectedCodeReach();
    }


    static boolean _isParentedCollection(ManagedObject adapter) {
        
        //legacy of (FIXME not a perfect match)
        //getOid() instanceof ParentedOid;
        
        return adapter.getSpecification().getBeanSort().isCollection();
    }

    static class EmptyUtil {
        private static final ManagedObject EMPTY = new ManagedObject() {

            @Override
            public ObjectSpecification getSpecification() {
                throw _Exceptions.unsupportedOperation();
            }

            @Override
            public Object getPojo() {
                return null;
            }
            
        };
    }
    
    static ManagedObject empty() {
        return EmptyUtil.EMPTY;
    }


}
