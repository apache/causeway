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

package org.apache.isis.metamodel.adapter;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.base._Tuples.Tuple2;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.ParentedOid;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.commons.ClassExtensions;
import org.apache.isis.metamodel.commons.ListExtensions;
import org.apache.isis.metamodel.commons.MethodExtensions;
import org.apache.isis.metamodel.commons.MethodUtil;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.consent.InteractionResult;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.interactions.InteractionUtils;
import org.apache.isis.metamodel.interactions.ObjectVisibilityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

/**
 * Adapters to domain objects, where the application is written in terms of
 * domain objects and those objects are represented within the NOF through these
 * adapter, and not directly.
 */
public interface ObjectAdapter extends ManagedObject {

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
    Oid getOid(); //XXX[2033] referenced by 'metamodel' only to create a bookmark (CommandUtil)

    /**
     * Returns either itself (if this is a root) or for parented collections, the
     * adapter corresponding to their {@link ParentedOid#getParentOid() root oid}.
     */
    ObjectAdapter getAggregateRoot(); //XXX[2033] not referenced by 'metamodel'

    Version getVersion(); //XXX[2033] not referenced by 'metamodel'
    void setVersion(Version version); //XXX[2033] not referenced by 'metamodel'


    /**
     * Whether this instance belongs to another object (meaning its
     * {@link #getOid()} will be a {@link ParentedOid}).
     */
    default boolean isParentedCollection() {
        return getOid() instanceof ParentedOid;
    }

    /**
     * Whether this is a value (standalone, has no oid).
     */
    default public boolean isValue() {
        return getOid().isValue();
    }

    default public ObjectAdapter injectServices(ServiceInjector serviceInjector) {
        if(isValue()) {
            return this; // guard against value objects
        }
        serviceInjector.injectServicesInto(getPojo());
        return this;
    }

    public final class Util {

        private Util() {}

        @Deprecated
        public static Object unwrapPojo(final ManagedObject adapter) {
            return ManagedObject.unwrapPojo(adapter);
        }

        @Deprecated// duplicate is in ManagedObject
        public static Object[] unwrapPojoArray(final ManagedObject[] adapters) {
            return ManagedObject.unwrapPojoArray(adapters);
        }

        public static List<Object> unwrapPojoList(final List<? extends ManagedObject> adapters) {
            List<Object> objects = _Lists.newArrayList();
            for (ManagedObject adapter : adapters) {
                objects.add(unwrapPojo(adapter));
            }
            return objects;
        }

        @SuppressWarnings("unchecked")
        public static <T> List<T> unwrapTypedPojoList(final List<? extends ManagedObject> adapters) {
            return (List<T>) unwrapPojoList(adapters);
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

        public static String titleString(final ManagedObject adapter) {
            return adapter != null ? adapter.titleString(null) : "";
        }

        public static boolean exists(final ManagedObject adapter) {
            return adapter != null && adapter.getPojo() != null;
        }

        public static boolean wrappedEqual(final ManagedObject adapter1, final ManagedObject adapter2) {
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
            return adapter1.getPojo().equals(adapter2.getPojo());
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
            if (obj1 instanceof ManagedObject && obj2 instanceof ManagedObject) {
                final ManagedObject adapterObj1 = (ManagedObject) obj1;
                final ManagedObject adapterObj2 = (ManagedObject) obj2;
                return nullSafeEquals(adapterObj1.getPojo(), adapterObj2.getPojo());
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
        public static List<ManagedObject> visibleAdapters(
                final ManagedObject collectionAdapter,
                final InteractionInitiatedBy interactionInitiatedBy) {

            final Stream<ManagedObject> objectAdapters = 
                    CollectionFacet.Utils.streamAdapters(collectionAdapter);

            return visibleAdapters(objectAdapters, interactionInitiatedBy);
        }

        public static List<ManagedObject> visibleAdapters(
                final Stream<ManagedObject> objectAdapters,
                final InteractionInitiatedBy interactionInitiatedBy) {

            return objectAdapters
                    .filter(adapter->isVisible(adapter, interactionInitiatedBy))
                    .collect(Collectors.toList());
        }


        /**
         * @param adapter - an adapter around the domain object whose visibility is being checked
         * @param interactionInitiatedBy
         */
        public static boolean isVisible(
                final ManagedObject adapter,
                final InteractionInitiatedBy interactionInitiatedBy) {
            
            if(adapter == null) {
                // a choices list could include a null (eg example in ToDoItems#choices1Categorized()); want to show as "visible"
                return true;
            }
            if(ManagedObject._isDestroyed(adapter)) {
                return false;
            }
            if(interactionInitiatedBy == InteractionInitiatedBy.FRAMEWORK) { 
                return true; 
            }
            return isVisibleForUser(adapter);
        }

        private static boolean isVisibleForUser(final ManagedObject adapter) {
            final VisibilityContext<?> context = createVisibleInteractionContextForUser(adapter);
            final ObjectSpecification objectSpecification = adapter.getSpecification();
            final InteractionResult visibleResult = InteractionUtils.isVisibleResult(objectSpecification, context);
            return visibleResult.isNotVetoing();
        }

        private static VisibilityContext<?> createVisibleInteractionContextForUser(
                final ManagedObject objectAdapter) {
            
            return new ObjectVisibilityContext(
                    objectAdapter,
                    objectAdapter.getSpecification().getIdentifier(),
                    InteractionInitiatedBy.USER,
                    Where.OBJECT_FORMS);
        }
    }

    boolean isTransient();
    boolean isRepresentingPersistent();
    boolean isDestroyed();


    public final class InvokeUtils {

        private InvokeUtils() {
        }

        public static void invokeAll(final Collection<Method> methods, final ManagedObject adapter) {
            MethodUtil.invoke(methods, Util.unwrapPojo(adapter));
        }

        public static Object invoke(final Method method, final ManagedObject adapter) {
            return MethodExtensions.invoke(method, Util.unwrapPojo(adapter));
        }

        public static Object invoke(final Method method, final ManagedObject adapter, final Object arg0) {
            return MethodExtensions.invoke(method, Util.unwrapPojo(adapter), new Object[] {arg0});
        }

        public static Object invoke(final Method method, final ManagedObject adapter, final ManagedObject arg0Adapter) {
            return invoke(method, adapter, Util.unwrapPojo(arg0Adapter));
        }

        public static Object invoke(final Method method, final ManagedObject adapter, final ManagedObject[] argumentAdapters) {
            return MethodExtensions.invoke(method, Util.unwrapPojo(adapter), Util.unwrapPojoArray(argumentAdapters));
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

                    final ManagedObject adapter = ManagedObject.of((ObjectSpecification)null, object);
                    args.set(i, adapter);
                }
            }
        }

    }


}
