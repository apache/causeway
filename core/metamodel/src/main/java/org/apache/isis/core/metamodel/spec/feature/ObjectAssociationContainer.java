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

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationException;

public interface ObjectAssociationContainer {

    // -- ASSOCIATION LOOKUP, PROPERTIES/COLLECTIONS (INHERITANCE CONSIDERED)

    /**
     * Similar to {@link #getDeclaredAssociation(String, MixedIn)},
     * but also considering any inherited object members.
     *
     * @implSpec If not found on the current 'type' search for the 'nearest' match in super-types,
     * and if nothing found there, search the interfaces.
     */
    Optional<ObjectAssociation> getAssociation(String id, MixedIn mixedIn);

    default ObjectAssociation getAssociationElseFail(final String id, final MixedIn mixedIn) {
        return getAssociation(id, mixedIn)
                .orElseThrow(()->_Exceptions.noSuchElement("id=%s", id));
    }

    default Optional<OneToOneAssociation> getProperty(final String id, final MixedIn mixedIn) {
        return getAssociation(id, mixedIn)
                .filter(ObjectAssociation.Predicates.PROPERTIES)
                .map(OneToOneAssociation.class::cast);
    }

    default OneToOneAssociation getPropertyElseFail(final String id, final MixedIn mixedIn) {
        return getProperty(id, mixedIn)
                .orElseThrow(()->_Exceptions.noSuchElement("id=%s", id));
    }

    default Optional<OneToManyAssociation> getCollection(final String id, final MixedIn mixedIn) {
        return getAssociation(id, mixedIn)
                .filter(ObjectAssociation.Predicates.COLLECTIONS)
                .map(OneToManyAssociation.class::cast);
    }

    default OneToManyAssociation getCollectionElseFail(final String id, final MixedIn mixedIn) {
        return getCollection(id, mixedIn)
                .orElseThrow(()->_Exceptions.noSuchElement("id=%s", id));
    }

    // -- MIXED-IN INCLUDED

    default Optional<ObjectAssociation> getAssociation(final String id) {
        return getAssociation(id, MixedIn.INCLUDED);}
    default ObjectAssociation getAssociationElseFail(final String id) {
        return getAssociationElseFail(id, MixedIn.INCLUDED);}
    default Optional<OneToOneAssociation> getProperty(final String id) {
        return getProperty(id, MixedIn.INCLUDED);}
    default OneToOneAssociation getPropertyElseFail(final String id) {
        return getPropertyElseFail(id, MixedIn.INCLUDED);}
    default Optional<OneToManyAssociation> getCollection(final String id) {
        return getCollection(id, MixedIn.INCLUDED);}
    default OneToManyAssociation getCollectionElseFail(final String id) {
        return getCollectionElseFail(id, MixedIn.INCLUDED);}

    // -- ASSOCIATION LOOKUP, DECLARED PROPERTIES/COLLECTIONS (NO INHERITANCE CONSIDERED)

    /**
     * Get the field object representing the field with the specified field
     * identifier, that is the association with the given
     * {@link ObjectAssociation#getId() id}.
     *
     * Throw a {@link ObjectSpecificationException} if no such association
     * exists.
     */
    Optional<ObjectAssociation> getDeclaredAssociation(String id, MixedIn mixedIn);

    // -- ASSOCIATION STREAMS (INHERITANCE CONSIDERED)

    /**
     * Same as {@link #streamDeclaredAssociations(MixedIn)}, but also considering any inherited object members.
     * @param contributed
     *
     * @implSpec Walk through the type hierarchy nearest to farthest and add any ObjectAssociation to the stream,
     * except don't add ObjectAssociations that already have been added (due to inheritance).
     */
    Stream<ObjectAssociation> streamAssociations(MixedIn mixedIn);


    /**
     * All {@link ObjectAssociation association}s that represent
     * {@link OneToOneAssociation properties}.
     */
    default Stream<OneToOneAssociation> streamProperties(final MixedIn mixedIn) {
        return streamAssociations(mixedIn)
                .filter(ObjectAssociation.Predicates.PROPERTIES)
                .map(OneToOneAssociation.class::cast);
    }

    /**
     * All {@link ObjectAssociation association}s that represents
     * {@link OneToManyAssociation collections}.
     */
    default Stream<OneToManyAssociation> streamCollections(final MixedIn mixedIn){
        return streamAssociations(mixedIn)
                .filter(ObjectAssociation.Predicates.COLLECTIONS)
                .map(OneToManyAssociation.class::cast);
    }

    /**
     * Properties visible as columns honoring order and visibility.
     */
    Stream<OneToOneAssociation> streamPropertiesForColumnRendering(
            Identifier memberIdentifier,
            ManagedObject parentObject);

    // -- ASSOCIATION STREAMS (INHERITANCE NOT CONSIDERED)

    /**
     * Return all the fields that exist in an object of this specification,
     * although they need not all be accessible or visible.
     *
     * To get the statically visible fields (where any invisible and
     * unauthorized fields have been removed) use
     * <tt>ObjectAssociationFilters#staticallyVisible(...)</tt>
     */
    Stream<ObjectAssociation> streamDeclaredAssociations(final MixedIn mixedIn);


}
