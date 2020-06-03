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

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.core.commons.internal.assertions._Assert;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * A collection of utilities for {@link ManagedObject}. 
 * @since 2.0
 *
 */
@UtilityClass
public final class ManagedObjects {
    
    // -- CATEGORISATION

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
     * that is the type is 'identifiable' (aka 'referencable' or 'bookmarkable') 
     */
    public static boolean isIdentifiable(@Nullable ManagedObject adapter) {
        return spec(adapter)
                .map(ObjectSpecification::isIdentifiable)
                .orElse(false);
    }
    
    public static boolean isEntity(ManagedObject adapter) {
        return spec(adapter)
                .map(ObjectSpecification::isEntity)
                .orElse(false);
    }

    public static boolean isValue(ManagedObject adapter) {
        return spec(adapter)
                .map(ObjectSpecification::isValue)
                .orElse(false);
    }
    
    public static Optional<String> getDomainType(ManagedObject adapter) {
        return spec(adapter)
                .map(ObjectSpecification::getSpecId)
                .map(ObjectSpecId::asString);
    }
    
    // -- IDENTIFICATION
    
    public static Optional<ObjectSpecification> spec(@Nullable ManagedObject adapter) {
        return isSpecified(adapter) ? Optional.of(adapter.getSpecification()) : Optional.empty(); 
    }
    
    public static Optional<RootOid> identify(@Nullable ManagedObject adapter) {
        return isSpecified(adapter) ? adapter.getRootOid() : Optional.empty(); 
    }
    
    public static RootOid identifyElseFail(@Nullable ManagedObject adapter) {
        return identify(adapter)
                .orElseThrow(()->_Exceptions.illegalArgument("cannot identify %s", adapter));
    }
    
    public static Optional<Bookmark> bookmark(@Nullable ManagedObject adapter) {
        return identify(adapter)
                .map(RootOid::asBookmark);
    }
    
    public static Bookmark bookmarkElseFail(@Nullable ManagedObject adapter) {
        return bookmark(adapter)
                .orElseThrow(()->_Exceptions.illegalArgument("cannot bookmark %s", adapter));
    }
    
    public static Optional<String> stringify(@Nullable ManagedObject adapter) {
        return identify(adapter)
                .map(RootOid::enString);
    }
    
    public static String stringifyElseFail(@Nullable ManagedObject adapter) {
        return stringify(adapter)
                .orElseThrow(()->_Exceptions.illegalArgument("cannot stringify %s", adapter));
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
    
    // -- TITLE UTILITIES
    
    public static String abbreviatedTitleOf(ManagedObject adapter, int maxLength, String suffix) {
        return abbreviated(titleOf(adapter), maxLength, suffix);
    }
    
    private static String titleOf(ManagedObject adapter) {
        return adapter!=null?adapter.titleString(null):"";
    }

    private static String abbreviated(final String str, final int maxLength, String suffix) {
        return str.length() < maxLength ? str : str.substring(0, maxLength - 3) + suffix;
    }

    // -- ENTITY UTILITIES
    
    /**
     * @param managedObject
     * @return managedObject
     * @throws AssertionError if managedObject is a detached entity  
     */
    public static ManagedObject requiresAttached(@NonNull ManagedObject managedObject) {
        val entityState = ManagedObject._entityState(managedObject);
        if(entityState.isPersistable()) {
            // ensure we have an attached entity
            _Assert.assertEquals(
                    EntityState.PERSISTABLE_ATTACHED, 
                    entityState,
                    ()-> String.format("entity %s is required to be attached (not detached) at this stage", 
                            managedObject.getSpecification().getSpecId()));
        }
        return managedObject;
    }
    
    @Nullable
    public static ManagedObject reattach(@Nullable ManagedObject managedObject) {
        if(isNullOrUnspecifiedOrEmpty(managedObject)) {
            return managedObject;
        }
        val entityState = ManagedObject._entityState(managedObject);
        if(!entityState.isPersistable()) {
            return managedObject;
        }
        if(!entityState.isDetached()) {
            return managedObject;
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


}
