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

package org.apache.isis.core.metamodel.spec.feature;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationException;

public interface ObjectAssociationContainer {
    
    // -- ASSOCIATION LOOKUP, PROPERTIES/COLLECTIONS (INHERITENCE CONSIDERED)
    
    /**
     * Same as {@link #getAssociation(String)}, but also considering any inherited object members. 
     * @param id
     * 
     * @implSpec If not found on the current 'type' search for the 'nearest' match in super-types, 
     * and if nothing found there, search the interfaces.  
     */
    Optional<ObjectAssociation> findAssociation(String id);
    
    default ObjectAssociation findAssociationElseFail(String id) {
        return findAssociation(id)
                .orElseThrow(()->_Exceptions.noSuchElement("id=%s", id));
    }

    // -- ASSOCIATION LOOKUP, DECLARED PROPERTIES/COLLECTIONS (NO INHERITENCE CONSIDERED)

    /**
     * Get the field object representing the field with the specified field
     * identifier.
     *
     * Throw a {@link ObjectSpecificationException} if no such association
     * exists.
     */
    Optional<ObjectAssociation> getAssociation(String id);
    
    default ObjectAssociation getAssociationElseFail(String id) {
        return getAssociation(id)
                .orElseThrow(()->_Exceptions.noSuchElement("id=%s", id));
    }
    

    /**
     * Return all the fields that exist in an object of this specification,
     * although they need not all be accessible or visible.
     *
     * To get the statically visible fields (where any invisible and
     * unauthorized fields have been removed) use
     * <tt>ObjectAssociationFilters#staticallyVisible(...)</tt>
     *
     */
    Stream<ObjectAssociation> streamAssociations(MixedIn contributed);

    /**
     * All {@link ObjectAssociation association}s that represent
     * {@link OneToOneAssociation properties}.
     * 
     */
    default Stream<OneToOneAssociation> streamProperties(MixedIn contributed) {
        return streamAssociations(contributed)
                .filter(ObjectAssociation.Predicates.PROPERTIES)
                .map(x->(OneToOneAssociation)x);
    }

    /**
     * All {@link ObjectAssociation association}s that represents
     * {@link OneToManyAssociation collections}.
     *
     * @return
     * 
     */
    default Stream<OneToManyAssociation> streamCollections(MixedIn contributed){
        return streamAssociations(contributed)
                .filter(ObjectAssociation.Predicates.COLLECTIONS)
                .map(x->(OneToManyAssociation)x);
    }

}
