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

package org.apache.isis.core.metamodel.spec.feature.memento;

import java.io.Serializable;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;

/**
 * {@link Serializable} representation of a {@link OneToManyAssociation}
 * (a parented collection of entities).
 *
 * @since 2.0 {index}
 */
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CollectionMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LogicalType owningType;

    /**
     * The id of the collection as referenced
     * from the parent object (eg <tt>lineItems</tt>).
     */
    @Getter private final String id;

    /**
     * {@link OneToManyAssociation#getId() id} of the {@link OneToManyAssociation collection}
     * passed into the constructor.
     *
     * <p>
     *     Is (I think) the same value as {@link #getId()}, though derived more directly.
     * </p>
     */
    @Getter private final String collectionId;
    @Getter private final String collectionName;

    public static CollectionMemento forCollection(final OneToManyAssociation collection) {
        return new CollectionMemento(
                parentObjectSpecFor(collection).getLogicalType(),
                collection.getIdentifier().getMemberName(),
                collection.getId(),
                collection.getName(),
                collection);
    }

    // -- LOAD/UNMARSHAL

    private transient OneToManyAssociation collection;

    public OneToManyAssociation getCollection(final SpecificationLoader specificationLoader) {
        if (collection == null) {
            collection = specificationLoader.specForLogicalTypeElseFail(owningType)
                    .getCollectionElseFail(id);
        }
        return collection;
    }

    // -- HELPER

    @Deprecated
    private static ObjectSpecification parentObjectSpecFor(OneToManyAssociation collection) {
        val result = collection.getMetaModelContext().getSpecificationLoader()
                .specForLogicalTypeElseFail(collection.getIdentifier().getLogicalType());

        //TODO simplify based on ...
        _Assert.assertEquals(result, collection.getOnType());

        return result;
    }


}
