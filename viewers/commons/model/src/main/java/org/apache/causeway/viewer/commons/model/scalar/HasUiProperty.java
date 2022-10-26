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
package org.apache.causeway.viewer.commons.model.scalar;

import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

@FunctionalInterface
public interface HasUiProperty extends UiProperty {

    UiProperty getUiProperty();

    @Override
    default ManagedObject getOwner() {
        return getUiProperty().getOwner();
    }

    @Override
    default OneToOneAssociation getMetaModel() {
        return getUiProperty().getMetaModel();
    }

    @Override
    default PropertyNegotiationModel getPendingPropertyModel() {
        return getUiProperty().getPendingPropertyModel();
    }

    @Override
    default ManagedProperty getManagedProperty() {
        return getUiProperty().getManagedProperty();
    }

    @Override
    default boolean whetherHidden() {
        return getUiProperty().whetherHidden();
    }

    @Override
    default String disableReasonIfAny() {
        return getUiProperty().disableReasonIfAny();
    }

}
