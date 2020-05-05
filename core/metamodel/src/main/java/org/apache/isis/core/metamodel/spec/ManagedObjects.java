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

import javax.annotation.Nullable;

import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * A collection of utilities for {@link ManagedObject}. 
 * @since 2.0
 *
 */
@UtilityClass
public final class ManagedObjects {

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
    public static ManagedObject copyOfIfClonable(@Nullable ManagedObject adapter) {

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


}
