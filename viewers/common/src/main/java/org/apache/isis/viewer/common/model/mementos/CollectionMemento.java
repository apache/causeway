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

package org.apache.isis.viewer.common.model.mementos;

import java.io.Serializable;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.Getter;
import lombok.val;

/**
 * {@link Serializable} representation of a {@link OneToManyAssociation}
 * (a parented collection of entities).
 */
public class CollectionMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter private final LogicalType owningType;
    private final String id;
    private final String collectionId;
    private final String collectionName;

    public CollectionMemento(final OneToManyAssociation collection) {
        this(parentObjectSpecFor(collection).getLogicalType(),
                collection.getIdentifier().getMemberName(),
                collection);
    }

    private CollectionMemento(
            final LogicalType owningType,
            final String id,
            final OneToManyAssociation collection) {
        this.owningType = owningType;
        this.id = id;
        this.collection = collection;
        this.collectionId = collection.getId();
        this.collectionName = collection.getName();
    }

    /**
     * The id of the collection as referenced
     * from the parent object (eg <tt>lineItems</tt>).
     */
    public String getId() {
        return id;
    }

    /**
     * {@link OneToManyAssociation#getId() id} of the {@link OneToManyAssociation collection}
     * passed into the constructor.
     *
     * <p>
     *     Is (I think) the same value as {@link #getId()}, though derived more directly.
     * </p>
     */
    public String getCollectionId() {
        return collectionId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    private transient OneToManyAssociation collection;

    public OneToManyAssociation getCollection(final SpecificationLoader specificationLoader) {
        if (collection == null) {
            collection = specificationLoader.specForLogicalTypeElseFail(owningType)
                    .getCollectionElseFail(id);
        }
        return collection;
    }

    // -- HELPER

    private static ObjectSpecification parentObjectSpecFor(final OneToManyAssociation collection) {
        val specificationLoader = collection.getMetaModelContext().getSpecificationLoader();
        val logicalType = collection.getIdentifier().getLogicalType();
        return specificationLoader.specForLogicalTypeElseFail(logicalType);
    }

}
