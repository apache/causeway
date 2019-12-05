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

package org.apache.isis.metamodel.spec;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.domain.DomainObjectList;
import org.apache.isis.commons.internal.base._Tuples.Tuple2;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.metamodel.adapter.oid.ParentedOid;
import org.apache.isis.metamodel.context.MetaModelContext;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.commons.ClassExtensions;
import org.apache.isis.metamodel.commons.ListExtensions;
import org.apache.isis.metamodel.commons.MethodExtensions;
import org.apache.isis.metamodel.commons.MethodUtil;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.metamodel.interactions.InteractionUtils;
import org.apache.isis.metamodel.interactions.ObjectVisibilityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.objectmanager.create.ObjectCreator;
import org.apache.isis.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.SpecificationLoaderDefault;

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
    final static class SimpleManagedObject implements ManagedObject {
        @NonNull private final ObjectSpecification specification;
        @NonNull private final Object pojo;
    }

    // -- LAZY

    @ToString(of = {"specification", "pojo"}) @EqualsAndHashCode(of = "pojo")
    final static class LazyManagedObject implements ManagedObject {

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

    default String titleString(ManagedObject contextAdapterIfAny) {
        return TitleUtil.titleString(this, contextAdapterIfAny);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class TitleUtil {

        public static String titleString(ManagedObject managedObject, ManagedObject contextAdapterIfAny) {
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

    public static Object unwrapPojo(final ManagedObject adapter) {
        return adapter != null ? adapter.getPojo() : null;
    }

    public static Object[] unwrapPojoArray(final ManagedObject[] adapters) {
        if (adapters == null) {
            return null;
        }
        final Object[] unwrappedObjects = new Object[adapters.length];
        int i = 0;
        for (final ManagedObject adapter : adapters) {
            unwrappedObjects[i++] = unwrapPojo(adapter);
        }
        return unwrappedObjects;
    }

    public static String unwrapPojoStringElse(final ManagedObject adapter, String orElse) {
        final Object obj = ManagedObject.unwrapPojo(adapter);
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof String)) {
            return orElse;
        }
        return (String) obj;
    }

    public static List<Object> unwrapPojoListElseEmpty(Collection<? extends ManagedObject> adapters) {
        if (adapters == null) {
            return Collections.emptyList();
        }
        return adapters.stream()
                .map(ManagedObject::unwrapPojo)
                .collect(Collectors.toList());
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

    static boolean isBookmarkable(ManagedObject adapter) {
        if(adapter==null) {
            return false;
        }
        val spec = adapter.getSpecification();
        if(spec.isManagedBean() || spec.isViewModel() || spec.isEntity()) {
            // services and view models are book-markable
            return true;
        }
        return false;
    }

    static boolean isNull(ManagedObject adapter) {
        if(adapter==null) {
            return true;
        }
        return adapter.getPojo()==null;
    }

    // -- VISIBILITY UTILITIES

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class VisibilityUtil {

        public static Predicate<? super ManagedObject> filterOn(InteractionInitiatedBy interactionInitiatedBy) {
            return $->ManagedObject.VisibilityUtil.isVisible($, interactionInitiatedBy);
        }

        /**
         * Filters a collection (an adapter around either a Collection or an Object[]) and returns a list of
         * {@link ManagedObject}s of those that are visible (as per any facet(s) installed on the element class
         * of the collection).
         *  @param collectionAdapter - an adapter around a collection (as returned by a getter of a collection, or of an autoCompleteNXxx() or choicesNXxx() method, etc
         * @param interactionInitiatedBy
         */
        public static List<ManagedObject> visibleAdapters(
                final ManagedObject collectionAdapter,
                final InteractionInitiatedBy interactionInitiatedBy) {
    
            return CollectionFacet.Utils.streamAdapters(collectionAdapter)
            .filter(VisibilityUtil.filterOn(interactionInitiatedBy))
            .collect(Collectors.toList());
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
    
        public static void invokeAll(final Collection<Method> methods, final ManagedObject adapter) {
            MethodUtil.invoke(methods, unwrapPojo(adapter));
        }
    
        public static Object invoke(final Method method, final ManagedObject adapter) {
            return MethodExtensions.invoke(method, unwrapPojo(adapter));
        }
    
        public static Object invoke(final Method method, final ManagedObject adapter, final Object arg0) {
            return MethodExtensions.invoke(method, unwrapPojo(adapter), new Object[] {arg0});
        }
    
        public static Object invoke(final Method method, final ManagedObject adapter, final ManagedObject arg0Adapter) {
            return invoke(method, adapter, unwrapPojo(arg0Adapter));
        }
    
        public static Object invoke(final Method method, final ManagedObject adapter, final ManagedObject[] argumentAdapters) {
            return MethodExtensions.invoke(method, unwrapPojo(adapter), unwrapPojoArray(argumentAdapters));
        }
    
        public static Object invokeC(final Method method, final ManagedObject adapter, 
                final Stream<Tuple2<Integer, ? extends ManagedObject>> paramsAndIndexes) {
            return invoke(method, adapter, asArray(paramsAndIndexes, method.getParameterTypes().length));
        }
    
        private static ManagedObject[] asArray(final Stream<Tuple2<Integer, ? extends ManagedObject>> paramsAndIndexes, int length) {
            final ManagedObject[] args = new ManagedObject[length];
            paramsAndIndexes.forEach(entry->{
                final Integer paramNum = entry.get_1();
                if(paramNum < length) {
                    args[paramNum] = entry.get_2();
                }
            });
            return args;
        }
    
        /**
         * Invokes the method, adjusting arguments as required to make them fit the method's parameters.
         *
         * <p>
         * That is:
         * <ul>
         * <li>if the method declares parameters but arguments are missing, then will provide 'null' defaults for these.
         * <li>if the method does not declare all parameters for arguments, then truncates arguments.
         * </ul>
         */
        public static Object invokeAutofit(
                final Method method, 
                final ManagedObject target, 
                List<? extends ManagedObject> argumentsIfAvailable/*, 
                final SpecificationLoader specLoader*/) {
    
            final List<ManagedObject> args = _Lists.newArrayList();
            if(argumentsIfAvailable != null) {
                args.addAll(argumentsIfAvailable);
            }
    
            adjust(method, args/*, specLoader*/);
    
            final ManagedObject[] argArray = args.toArray(new ManagedObject[]{});
            return invoke(method, target, argArray);
        }
    
        private static void adjust(
                final Method method, final List<ManagedObject> args /*, final SpecificationLoader specLoader*/) {
    
            final Class<?>[] parameterTypes = method.getParameterTypes();
            ListExtensions.adjust(args, parameterTypes.length);
    
            for(int i=0; i<parameterTypes.length; i++) {
                final Class<?> cls = parameterTypes[i];
                if(args.get(i) == null && cls.isPrimitive()) {
                    final Object object = ClassExtensions.toDefault(cls);
    
                    final ManagedObject adapter = of((ObjectSpecification)null, object);
                    args.set(i, adapter);
                }
            }
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

        if(adapter==null || adapter.getPojo()==null) {
            return null;
        }

        val spec = adapter.getSpecification();
        if(!spec.isEntity()) {
            return EntityState.not_Persistable;
        }

        val entityFacet = spec.getFacet(EntityFacet.class);
        if(entityFacet==null) {
            throw _Exceptions.unrecoverable("Entity types must have an EntityFacet");
        }

        return entityFacet.getEntityState(adapter.getPojo());
    }

    static boolean _isDestroyed(ManagedObject adapter) {
        return _entityState(adapter) == EntityState.persistable_Destroyed;
    }

    @Deprecated
    static void _whenFirstIsBookmarkable_ensureSecondIsNotTransient(
            ManagedObject first,
            ManagedObject second) {

        if(ManagedObject.isBookmarkable(first) && second!=null) {

            val refSpec = second.getSpecification();

            if(refSpec.isParented() || !refSpec.isEntity()) {
                return;
            }

            val oid = ManagedObject._identify(second);

            if(oid.isTransient()) {

                // TODO: I've never seen this exception, and in any case DataNucleus supports persistence-by-reachability; so probably not required
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

    @Deprecated
    static void _makePersistentInTransaction(ManagedObject adapter) {

        // legacy of
        // getResourceContext().makePersistentInTransaction(adapter);

        val spec = adapter.getSpecification();
        if(spec.isEntity()) {
            val entityFacet = spec.getFacet(EntityFacet.class);
            entityFacet.persist(spec, adapter.getPojo());
            return;
        }

        throw _Exceptions.unexpectedCodeReach();
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

    @Deprecated
    static Stream<ManagedObject> _bulkLoadStream(Stream<RootOid> rootOids) {

        throw _Exceptions.notImplemented();

        //        PersistenceSession persistenceSession = IsisContext.getPersistenceSession()
        //                .orElseThrow(()->_Exceptions.unrecoverable("no PersistenceSession available"));
        //        
        //        final Map<RootOid, ObjectAdapter> adaptersByOid = persistenceSession.adaptersFor(rootOids);
        //        final Collection<ObjectAdapter> adapterList = adaptersByOid.values();
        //        return stream(adapterList)
        //                .filter(_NullSafe::isPresent)
        //                .map(ManagedObject.class::cast);
    }

    static boolean _isParentedCollection(ManagedObject adapter) {
        
        //legacy of (FIXME not a perfect match)
        //getOid() instanceof ParentedOid;
        
        return adapter.getSpecification().getBeanSort().isCollection();
    }




}
