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

import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;

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
     * @return
     */
    public static ManagedObject of(@NonNull ObjectSpecification specification, @Nullable Object pojo) {
        
        specification.assertPojoCompatible(pojo);
        
        return new SimpleManagedObject(specification, pojo);
    }
    
    /**
     * Optimized for cases, when the pojo's specification and rootOid are already available.
     */
    public static ManagedObject identified(ObjectSpecification specification, Object pojo, RootOid rootOid) {
        if(!specification.getCorrespondingClass().isAssignableFrom(pojo.getClass())) {
            throw _Exceptions.illegalArgument(
                    "Pojo not compatible with ObjectSpecification, " +
                    "objectSpec.correspondingClass = %s, " +
                    "pojo.getClass() = %s, " +
                    "pojo.toString() = %s",
                    specification.getCorrespondingClass(), pojo.getClass(), pojo.toString());
        }
        return SimpleManagedObject.identified(specification, pojo, rootOid);
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
    @RequiredArgsConstructor(staticName="of") 
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

        public Optional<RootOid> getRootOid() {
            return rootOidLazy.get();
        }
        
        // -- LAZY ID HANDLING
        private final _Lazy<Optional<RootOid>> rootOidLazy = 
                _Lazy.threadSafe(()->ManagedObjectInternalUtil.identify(this));  

        
    }

    // -- LAZY

    @EqualsAndHashCode(of = "pojo")
    static final class LazyManagedObject implements ManagedObject {

        @NonNull private final Function<Class<?>, ObjectSpecification> specLoader;  

        @Getter @NonNull private final Object pojo;
        
        @Getter(lazy=true, onMethod = @__(@Override)) 
        private final Optional<RootOid> rootOid = ManagedObjectInternalUtil.identify(this);

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

    // -- DEPRECATIONS (REFACTORING)

    static boolean _isParentedCollection(ManagedObject adapter) {
        
        //legacy of (FIXME not a perfect match)
        //getOid() instanceof ParentedOid;
        
        return adapter.getSpecification().getBeanSort().isCollection();
    }


}
