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
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * {@link Serializable} representation of a {@link OneToOneAssociation}
 *
 * @since 2.0 {index}
 */
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PropertyMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    @EqualsAndHashCode.Exclude
    @Getter private final @NonNull LogicalType owningType;

    //TODO equality by identifier required by EntityModel, need to investigate why
    @EqualsAndHashCode.Include
    @Getter private final @NonNull String identifier;

    @EqualsAndHashCode.Exclude
    @Getter private final @NonNull LogicalType type;

    // -- FACTORY

    public static PropertyMemento forProperty(final OneToOneAssociation property) {
        return new PropertyMemento(
                parentObjectSpecFor(property).getLogicalType(),
                property.getIdentifier().getMemberName(),
                property.getSpecification().getLogicalType(),
                property);
    }

    // -- LOAD/UNMARSHAL

    @EqualsAndHashCode.Exclude
    private transient OneToOneAssociation property;

    public OneToOneAssociation getProperty(final SpecificationLoader specLoader) {
        if (property == null) {
            property = specLoader.specForLogicalTypeElseFail(owningType)
                    .getPropertyElseFail(identifier);
        }
        return property;
    }

    @Override
    public String toString() {
        return getOwningType().getLogicalTypeName() + "#" + getIdentifier();
    }

    // -- HELPER

    @Deprecated
    private static ObjectSpecification parentObjectSpecFor(OneToOneAssociation property) {
        val result = property.getMetaModelContext().getSpecificationLoader()
                .specForLogicalTypeElseFail(property.getIdentifier().getLogicalType());

        //TODO simplify based on ...
        _Assert.assertEquals(result, property.getOnType());

        return result;
    }


}
