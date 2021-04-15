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

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
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
    
    /**
     * Returns the object id as identified by the ObjectManager. 
     * Entity IDs are considered immutable, hence will be memoized once fetched.
     */
    Optional<RootOid> getRootOid();
    
    boolean isRootOidMemoized();
    
    // -- TITLE

    public default String titleString() {
        return titleString(null);
    }

    default String titleString(@Nullable ManagedObject contextAdapterIfAny) {
        return ManagedObjectInternalUtil.titleString(this, contextAdapterIfAny);
    }
    
    // -- SHORTCUT - OBJECT MANAGER
    
    default ObjectManager getObjectManager() {
        return ManagedObjectInternalUtil.objectManager(this)
                .orElseThrow(()->_Exceptions
                        .illegalArgument("Can only retrieve ObjectManager from ManagedObjects "
                                + "that are 'specified'."));
    }

    // -- SHORTCUT - ELEMENT SPECIFICATION

    /**
     * Used only for (standalone or parented) collections.
     */
    default public Optional<ObjectSpecification> getElementSpecification() {
        return getSpecification().getElementSpecification();
    }

    // -- SHORTCUT - ICON NAME

    /**
     * Returns the name of an icon to use if this object is to be displayed
     * graphically.
     * <p>
     * May return <code>null</code> if no icon is specified.
     */
    default public String getIconName() {
        return getSpecification().getIconName(this);
    }

    // -- FACTORIES

    /**
     * Optimized for cases, when the pojo's specification is already available.
     * @param specification
     * @param pojo - might also be a collection of pojos
     */
    public static ManagedObject of(
            @NonNull ObjectSpecification specification, 
            @Nullable Object pojo) {
        
        ManagedObjects.assertPojoNotManaged(pojo);
        specification.assertPojoCompatible(pojo);
        
        //ISIS-2430 Cannot assume Action Param Spec to be correct when eagerly loaded
        //actual type in use (during runtime) might be a sub-class of the above
        if(pojo==null 
                || pojo.getClass().equals(specification.getCorrespondingClass())
                ) {
            // if actual type matches spec's, we assume, that we don't need to reload, 
            // so this is a shortcut for performance reasons  
            return SimpleManagedObject.of(specification, pojo);
        }
        
        //_Probe.errOut("upgrading spec %s on type %s", specification, pojo.getClass());
        //ManagedObjects.warnIfAttachedEntity(adapter, "consider using ManagedObject.identified(...) for entity");
        
        val specLoader = specification.getMetaModelContext().getSpecificationLoader();
        return ManagedObject.lazy(specLoader, pojo);
    }
    
    /**
     * Optimized for cases, when the pojo's specification and rootOid are already available.
     */
    public static ManagedObject identified(
            @NonNull ObjectSpecification specification, 
            @NonNull Object pojo, 
            @NonNull RootOid rootOid) {
        
        if(!specification.getCorrespondingClass().isAssignableFrom(pojo.getClass())) {
            throw _Exceptions.illegalArgument(
                    "Pojo not compatible with ObjectSpecification, " +
                    "objectSpec.correspondingClass = %s, " +
                    "pojo.getClass() = %s, " +
                    "pojo.toString() = %s",
                    specification.getCorrespondingClass(), pojo.getClass(), pojo.toString());
        }
        ManagedObjects.assertPojoNotManaged(pojo);
        return SimpleManagedObject.identified(specification, pojo, rootOid);
    }

    /**
     * For cases, when the pojo's specification is not available and needs to be looked up. 
     * @param specLoader
     * @param pojo
     */
    public static ManagedObject lazy(
            SpecificationLoader specLoader, 
            Object pojo) {
        ManagedObjects.assertPojoNotManaged(pojo);
        val adapter = new LazyManagedObject(cls->specLoader.specForType(cls).orElse(null), pojo);
        //ManagedObjects.warnIfAttachedEntity(adapter, "consider using ManagedObject.identified(...) for entity");
        return adapter;
    }
    
    // -- EMPTY
    
    /** has no ObjectSpecification and no value (pojo) */
    static ManagedObject unspecified() {
        return ManagedObjectInternalUtil.UNSPECIFIED;
    }
    
    /** has an ObjectSpecification, but no value (pojo) */
    static ManagedObject empty(@NonNull final ObjectSpecification spec) {
        return ManagedObject.of(spec, null);
    }

    // -- SIMPLE

    @Value 
    @RequiredArgsConstructor(staticName="of", access = AccessLevel.PRIVATE) 
    @EqualsAndHashCode(of = "pojo")
    @ToString(of = {"specification", "pojo"}) //ISIS-2317 make sure toString() is without side-effects
    static final class SimpleManagedObject implements ManagedObject {
        
        public static ManagedObject identified(
                @NonNull  final ObjectSpecification spec, 
                @Nullable final Object pojo, 
                @NonNull  final RootOid rootOid) {
            val managedObject = SimpleManagedObject.of(spec, pojo);
            managedObject.rootOidLazy.set(Optional.of(rootOid));
            return managedObject;
        }
        
        @NonNull private final ObjectSpecification specification;
        @Nullable private final Object pojo;

        @Override
        public Optional<RootOid> getRootOid() {
            return rootOidLazy.get();
        }
        
        // -- LAZY ID HANDLING
        private final _Lazy<Optional<RootOid>> rootOidLazy = 
                _Lazy.threadSafe(()->ManagedObjectInternalUtil.identify(this));

        @Override
        public boolean isRootOidMemoized() {
            return rootOidLazy.isMemoized();
        }  
        
    }

    // -- LAZY

    @EqualsAndHashCode(of = "pojo")
    static final class LazyManagedObject implements ManagedObject {

        @NonNull private final Function<Class<?>, ObjectSpecification> specLoader;  

        @Getter @NonNull private final Object pojo;
        
        @Override
        public Optional<RootOid> getRootOid() {
            return rootOidLazy.get();
        }
        
        // -- LAZY ID HANDLING
        private final _Lazy<Optional<RootOid>> rootOidLazy = 
                _Lazy.threadSafe(()->ManagedObjectInternalUtil.identify(this));
        
        @Override
        public boolean isRootOidMemoized() {
            return rootOidLazy.isMemoized();
        }  

        private final _Lazy<ObjectSpecification> specification = _Lazy.threadSafe(this::loadSpec);

        public LazyManagedObject(@NonNull Function<Class<?>, ObjectSpecification> specLoader, @NonNull Object pojo) {
            this.specLoader = specLoader;
            this.pojo = pojo;
        }
        
        @Override
        public ObjectSpecification getSpecification() {
            return specification.get();
        }

        @Override //ISIS-2317 make sure toString() is without side-effects
        public String toString() {
            if(specification.isMemoized()) {
                return String.format("ManagedObject[spec=%s, pojo=%s]",
                        ""+getSpecification(),
                        ""+getPojo());
            }
            return String.format("ManagedObject[spec=%s, pojo=%s]",
                    "[lazy not loaded]",
                    ""+getPojo());
        }
        
        private ObjectSpecification loadSpec() {
            return specLoader.apply(pojo.getClass());
        }

    }

    

}
