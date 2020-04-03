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
package org.apache.isis.incubator.viewer.vaadin.model.menu;

import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.incubator.viewer.vaadin.model.entity.EntityUiModel;

import lombok.Data;

@Data
public class ServiceAndActionUiModel {

    final EntityUiModel entityUiModel;
    final String serviceName;
    // TODO final ServiceActionLinkFactory linkAndLabelFactory;
    // TODO final EntityModel serviceEntityModel;
    final ObjectAction objectAction;
    final boolean isFirstSection;

//    Optional<String> cssClassFa() {
//        return Optional.ofNullable(Util.cssClassFaFor(objectAction));
//    }
//
//    Optional<String> cssClass(final ManagedObject managedObject) {
//        return Optional.ofNullable(Util.cssClassFor(objectAction, managedObject));
//    }
}
