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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.adapter.oid.factory.OidFactory;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;

import lombok.Getter;
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

    @Value @RequiredArgsConstructor(staticName="of") 
    final static class SimpleManagedObject implements ManagedObject {
        @NonNull private final ObjectSpecification specification;
        @NonNull private final Object pojo;
    }

    // -- LAZY
    
    @ToString(of = {"specification", "pojo"}) 
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
    
    //Function<Class<?>, ObjectSpecification> specLoader
    
    // -- TITLE

    public default String titleString() {
        return titleString(null);
    }

    default String titleString(ObjectAdapter contextAdapterIfAny) {
        return TitleUtil.titleString(this, contextAdapterIfAny);
    }


    public static class TitleUtil {

        public static String titleString(ManagedObject managedObject, ObjectAdapter contextAdapterIfAny) {
            if (managedObject.getSpecification().isParentedOrFreeCollection()) {
                final CollectionFacet facet = managedObject.getSpecification().getFacet(CollectionFacet.class);
                return collectionTitleString(managedObject, facet);
            } else {
                return objectTitleString(managedObject, contextAdapterIfAny);
            }
        }

        private static String objectTitleString(ManagedObject managedObject, ObjectAdapter contextAdapterIfAny) {
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
    
    // -- SHORTCUTS
    
//    /**
//     * Uses the currently available {@link SpecificationLoader} to
//     * @param pojo
//     * @return
//     * @apiNote its not well thought through yet what to do with null argument
//     */
//    public static ManagedObject forPojo(Object pojo) {
//        if(pojo==null) {
//            return null;
//        }
//        return of(MetaModelContext.current()::getSpecification, pojo);
//    }
    
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
    
    public static List<Object> unwrapPojoListElseEmpty(Collection<ManagedObject> adapters) {
        if (adapters == null) {
            return Collections.emptyList();
        }
        return adapters.stream()
            .map(ManagedObject::unwrapPojo)
            .collect(Collectors.toList());
    }

    // -- DEPRECATIONS (in an attempt to decouple the metamodel from ObjectAdapter)
    
    @Deprecated
    public static ObjectAdapter promote(ManagedObject managedObject) {
        if(managedObject==null) {
            return null;
        }
        return (ObjectAdapter) managedObject;
    }

    // -- DEPRECATIONS - SPECIALIZED
    
    @Deprecated
    static boolean _isDestroyed(ManagedObject adapter) {
        //TODO only applicable when persistable
        return ManagedObject.promote(adapter).isDestroyed();
    }

    @Deprecated
    static RootOid _rootOidIfAny(ManagedObject adapter) {
        // TODO Auto-generated method stub
        val oid = ManagedObject.promote(adapter).getOid();
        if(!(oid instanceof RootOid)) {
            return null;
        }
        return (RootOid) oid;
    }
    
    @Deprecated
    static RootOid _collectionOidIfAny(ManagedObject adapter) {
        // TODO Auto-generated method stub
        val oid = ManagedObject.promote(adapter).getOid();
        if(!(oid instanceof RootOid)) {
            return null;
        }
        return (RootOid) oid;
    }

    @Deprecated
    static boolean _whenFirstIsRepresentingPersistent_ensureSecondIsAsWell(
            ManagedObject first,
            ManagedObject second) {
        
        //if(ownerAdapter.getSpecification().isEntity() && !referencedAdapter.getSpecification().isEntity()) {
        
        if(ManagedObject.promote(first).isRepresentingPersistent() &&
                ManagedObject.promote(second).isTransient()) {
            return false; 
        }
        return true;
    }
    
    static final class Oids {
        static final OidFactory oidFactory = OidFactory.buildDefault();
    }

    static Oid _oid(ManagedObject adapter) {
        if(adapter instanceof ObjectAdapter) {
            return promote(adapter).getOid();
        }
        
        return Oids.oidFactory.oidFor(adapter);
    }





}
