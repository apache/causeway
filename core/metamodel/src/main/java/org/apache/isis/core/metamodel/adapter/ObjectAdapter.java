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

package org.apache.isis.core.metamodel.adapter;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.base._Tuples.Tuple2;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.lang.ClassExtensions;
import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.commons.lang.MethodExtensions;
import org.apache.isis.core.commons.lang.MethodUtil;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.ParentedCollectionOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectVisibilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ElementSpecificationProvider;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Specification;

/**
 * Adapters to domain objects, where the application is written in terms of
 * domain objects and those objects are represented within the NOF through these
 * adapter, and not directly.
 */
public interface ObjectAdapter extends Instance {

    /**
     * Refines {@link Instance#getSpecification()}.
     */
    @Override
    ObjectSpecification getSpecification();

    /**
     * Returns the adapted domain object, the POJO, that this adapter represents
     * with the framework.
     */
    @Override
    Object getObject();

    /**
     * Returns the title to display this object with, usually obtained from
     * the wrapped {@link #getObject() domain object}.
     *
     * @deprecated - use {@link #titleString(ObjectAdapter)}
     */
    @Deprecated
    String titleString();

    /**
     * Returns the title to display this object with, rendered within the context
     * of some other adapter.
     *
     * <p>
     * @see TitleFacet#title(ObjectAdapter, ObjectAdapter)
     */
    String titleString(ObjectAdapter contextAdapter);

    /**
     * Return an {@link Instance} of the specified {@link Specification} with
     * respect to this {@link ObjectAdapter}.
     *
     * <p>
     * If called with {@link ObjectSpecification}, then just returns
     * <tt>this</tt>). If called for other subinterfaces, then should provide an
     * appropriate {@link Instance} implementation.
     *
     * <p>
     * Designed to be called in a double-dispatch design from
     * {@link Specification#getInstance(ObjectAdapter)}.
     *
     * <p>
     * Note: this method will throw an {@link UnsupportedOperationException}
     * unless the extended <tt>PojoAdapterXFactory</tt> is configured. (That is,
     * only <tt>PojoAdapterX</tt> provides support for this; the regular
     * <tt>PojoAdapter</tt> does not currently.
     *
     * @return
     */
    Instance getInstance(Specification specification);

    /**
     * For (stand-alone) collections, returns the element type.
     *
     * <p>
     * For owned (aggregated) collections, the element type can be determined
     * from the <tt>TypeOfFacet</tt> associated with the
     * <tt>ObjectAssociation</tt> representing the collection.
     *
     * @see #setElementSpecificationProvider(ElementSpecificationProvider)
     */
    ObjectSpecification getElementSpecification();

    /**
     * For (stand-alone) collections, returns the element type.
     *
     * @see #getElementSpecification()
     */
    void setElementSpecificationProvider(ElementSpecificationProvider elementSpecificationProvider);




    /**
     * Returns the name of an icon to use if this object is to be displayed
     * graphically.
     *
     * <p>
     * May return <code>null</code> if no icon is specified.
     */
    String getIconName();

    /**
     * Checks the version of this adapter to make sure that it does not differ
     * from the specified version.
     *
     * @throws ConcurrencyException
     *             if the specified version differs from the version held this
     *             adapter.
     */
    void checkLock(Version version);

    /**
     * The object's unique {@link Oid}.
     *
     * <p>
     * This id allows the object to added to, stored by,
     * and retrieved from the object store.  Objects can be looked up by their
     * {@link Oid}.
     *
     * <p>
     * Note that standalone value objects ("foobar", or 5, or a date),
     * are not mapped and have a <tt>null</tt> oid.
     */
    Oid getOid();

    /**
     * Returns either itself (if this is a root) or the parented collections, the
     * adapter corresponding to their {@link ParentedCollectionOid#getRootOid() root oid}.
     */
    ObjectAdapter getAggregateRoot();

    Version getVersion();
    void setVersion(Version version);


    /**
     * Whether this instance belongs to another object (meaning its
     * {@link #getOid()} will be a {@link ParentedCollectionOid}).
     */
    default boolean isParentedCollection() {
        return getOid() instanceof ParentedCollectionOid;
    }

    /**
     * Whether this is a value (standalone, has no oid).
     */
    default public boolean isValue() {
        return getOid().isValue();
    }


    public final class Util {

        private Util() {}

        public static Object unwrap(final Instance adapter) {
            return adapter != null ? adapter.getObject() : null;
        }

        public static Object[] unwrap(final Instance[] adapters) {
            if (adapters == null) {
                return null;
            }
            final Object[] unwrappedObjects = new Object[adapters.length];
            int i = 0;
            for (final Instance adapter : adapters) {
                unwrappedObjects[i++] = unwrap(adapter);
            }
            return unwrappedObjects;
        }

        public static List<Object> unwrap(final List<? extends Instance> adapters) {
            List<Object> objects = _Lists.newArrayList();
            for (Instance adapter : adapters) {
                objects.add(unwrap(adapter));
            }
            return objects;
        }

        @SuppressWarnings("unchecked")
        public static <T> List<T> unwrapT(final List<? extends Instance> adapters) {
            return (List<T>) unwrap(adapters);
        }

        public static String unwrapAsString(final ObjectAdapter adapter) {
            final Object obj = unwrap(adapter);
            if (obj == null) {
                return null;
            }
            if (!(obj instanceof String)) {
                return null;
            }
            return (String) obj;
        }

        public static String titleString(final ObjectAdapter adapter) {
            return adapter != null ? adapter.titleString(null) : "";
        }

        public static boolean exists(final ObjectAdapter adapter) {
            return adapter != null && adapter.getObject() != null;
        }

        public static boolean wrappedEqual(final ObjectAdapter adapter1, final ObjectAdapter adapter2) {
            final boolean defined1 = exists(adapter1);
            final boolean defined2 = exists(adapter2);
            if (defined1 && !defined2) {
                return false;
            }
            if (!defined1 && defined2) {
                return false;
            }
            if (!defined1 && !defined2) {
                return true;
            } // both null
            return adapter1.getObject().equals(adapter2.getObject());
        }

        public static boolean nullSafeEquals(final Object obj1, final Object obj2) {
            if (obj1 == null && obj2 == null) {
                return true;
            }
            if (obj1 == null || obj2 == null) {
                return false;
            }
            if (obj1.equals(obj2)) {
                return true;
            }
            if (obj1 instanceof ObjectAdapter && obj2 instanceof ObjectAdapter) {
                final ObjectAdapter adapterObj1 = (ObjectAdapter) obj1;
                final ObjectAdapter adapterObj2 = (ObjectAdapter) obj2;
                return nullSafeEquals(adapterObj1.getObject(), adapterObj2.getObject());
            }
            return false;
        }

        /**
         * Filters a collection (an adapter around either a Collection or an Object[]) and returns a list of
         * {@link ObjectAdapter}s of those that are visible (as per any facet(s) installed on the element class
         * of the collection).
         *  @param collectionAdapter - an adapter around a collection (as returned by a getter of a collection, or of an autoCompleteNXxx() or choicesNXxx() method, etc
         * @param interactionInitiatedBy
         */
        public static List<ObjectAdapter> visibleAdapters(
                final ObjectAdapter collectionAdapter,
                final InteractionInitiatedBy interactionInitiatedBy) {

            final CollectionFacet facet = CollectionFacet.Utils.getCollectionFacetFromSpec(collectionAdapter);
            Iterable<ObjectAdapter> objectAdapters = facet.iterable(collectionAdapter);

            return visibleAdapters(objectAdapters, interactionInitiatedBy);
        }

        /**
         * as per {@link #visibleAdapters(ObjectAdapter, InteractionInitiatedBy)}.
         *  @param objectAdapters - iterable over the respective adapters of a collection (as returned by a getter of a collection, or of an autoCompleteNXxx() or choicesNXxx() method, etc
         * @param interactionInitiatedBy
         */
        public static List<ObjectAdapter> visibleAdapters(
                final Iterable<ObjectAdapter> objectAdapters,
                final InteractionInitiatedBy interactionInitiatedBy) {
            final List<ObjectAdapter> adapters = _Lists.newArrayList();
            for (final ObjectAdapter adapter : objectAdapters) {
                final boolean visible = isVisible(adapter,
                        interactionInitiatedBy);
                if(visible) {
                    adapters.add(adapter);
                }
            }
            return adapters;
        }

        /**
         * @param adapter - an adapter around the domain object whose visibility is being checked
         * @param interactionInitiatedBy
         */
        public static boolean isVisible(
                final ObjectAdapter adapter,
                final InteractionInitiatedBy interactionInitiatedBy) {
            if(adapter == null) {
                // a choices list could include a null (eg example in ToDoItems#choices1Categorized()); want to show as "visible"
                return true;
            }
            if(adapter.isDestroyed()) {
                return false;
            }
            if(interactionInitiatedBy == InteractionInitiatedBy.FRAMEWORK) { return true; }
            return isVisibleForUser(adapter);
        }

        private static boolean isVisibleForUser(final ObjectAdapter adapter) {
            final VisibilityContext<?> context = createVisibleInteractionContextForUser(adapter);
            final ObjectSpecification objectSpecification = adapter.getSpecification();
            final InteractionResult visibleResult = InteractionUtils.isVisibleResult(objectSpecification, context);
            return visibleResult.isNotVetoing();
        }

        private static VisibilityContext<?> createVisibleInteractionContextForUser(
                final ObjectAdapter objectAdapter) {
            return new ObjectVisibilityContext(
                    objectAdapter,
                    objectAdapter.getSpecification().getIdentifier(),
                    InteractionInitiatedBy.USER,
                    Where.OBJECT_FORMS);
        }
    }

    boolean isTransient();
    boolean representsPersistent();
    boolean isDestroyed();


    public final class InvokeUtils {

        private InvokeUtils() {
        }

        public static void invokeAll(final Collection<Method> methods, final Instance adapter) {
            MethodUtil.invoke(methods, Util.unwrap(adapter));
        }

        public static Object invoke(final Method method, final Instance adapter) {
            return MethodExtensions.invoke(method, Util.unwrap(adapter));
        }

        public static Object invoke(final Method method, final Instance adapter, final Object arg0) {
            return MethodExtensions.invoke(method, Util.unwrap(adapter), new Object[] {arg0});
        }

        public static Object invoke(final Method method, final Instance adapter, final Instance arg0Adapter) {
            return invoke(method, adapter, Util.unwrap(arg0Adapter));
        }

        public static Object invoke(final Method method, final Instance adapter, final Instance[] argumentAdapters) {
            return MethodExtensions.invoke(method, Util.unwrap(adapter), Util.unwrap(argumentAdapters));
        }

        public static Object invokeC(final Method method, final Instance adapter, 
                final Stream<Tuple2<Integer, ? extends Instance>> paramsAndIndexes) {
            return invoke(method, adapter, asArray(paramsAndIndexes, method.getParameterTypes().length));
        }

        private static Instance[] asArray(final Stream<Tuple2<Integer, ? extends Instance>> paramsAndIndexes, int length) {
            final Instance[] args = new Instance[length];
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
                final Instance target, 
                List<? extends Instance> argumentsIfAvailable/*, 
                final SpecificationLoader specLoader*/) {
            
            final List<Instance> args = _Lists.newArrayList();
            if(argumentsIfAvailable != null) {
                args.addAll(argumentsIfAvailable);
            }

            adjust(method, args/*, specLoader*/);

            final Instance[] argArray = args.toArray(new Instance[]{});
            return invoke(method, target, argArray);
        }

        private static void adjust(
                final Method method, final List<Instance> args /*, final SpecificationLoader specLoader*/) {
            
            final Class<?>[] parameterTypes = method.getParameterTypes();
            ListExtensions.adjust(args, parameterTypes.length);

            for(int i=0; i<parameterTypes.length; i++) {
                final Class<?> cls = parameterTypes[i];
                if(args.get(i) == null && cls.isPrimitive()) {
                    final Object object = ClassExtensions.toDefault(cls);
                    
                    final Instance adapter = Instance.of((ObjectSpecification)null, object);
                    args.set(i, adapter);
                }
            }
        }

//        /**
//         * Invokes the method, adjusting arguments as required.
//         *
//         * <p>
//         * That is:
//         * <ul>
//         * <li>if the method declares parameters but no arguments are provided, then will provide 'null' defaults for these.
//         * <li>if the method does not declare parameters but arguments were provided, then will ignore those arguments.
//         * </ul>
//         */
//        private static Object invokeWithDefaults(final Method method, final Instance adapter, final Instance[] argumentAdapters) {
//            final int numParams = method.getParameterTypes().length;
//            Instance[] adapters;
//
//            if(argumentAdapters == null || argumentAdapters.length == 0) {
//                adapters = new Instance[numParams];
//            } else if(numParams == 0) {
//                // ignore any arguments, even if they were supplied.
//                // eg used by contributee actions, but
//                // underlying service 'default' action declares no params
//                adapters = new Instance[0];
//            } else if(argumentAdapters.length == numParams){
//                adapters = argumentAdapters;
//            } else {
//                throw new IllegalArgumentException("Method has " + numParams + " params but " + argumentAdapters.length + " arguments provided");
//            }
//
//            return invoke(method, adapter, adapters);
//        }
    }

    public static class Functions {

        private Functions(){}

        public static Function<ObjectAdapter, Object> getObject() {
            return Util::unwrap;
        }

    }

    /**
     * 
     * @param newOid
     * @return a copy of this adapter, having a new RootOid 
     * @since 2.0.0-M2
     */
    ObjectAdapter withOid(RootOid newOid);

    /**
     * 
     * @param newPojo
     * @return a copy of this adapter, having a new Pojo 
     * @since 2.0.0-M2
     */
    ObjectAdapter withPojo(Object newPojo);


}
