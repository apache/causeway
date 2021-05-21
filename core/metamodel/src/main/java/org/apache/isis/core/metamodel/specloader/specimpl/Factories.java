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
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.function.Function;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

/** package private utility */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Factories {

    // -- MIXINS

    static Function<ObjectActionDefault, ObjectActionMixedIn> mixedInAction(
            final ObjectSpecification mixinTypeSpec,
            final Class<?> mixinType,
            final String mixinMethodName) {

        return mixinTypeAction -> new ObjectActionMixedIn(
                mixinType, mixinMethodName, mixinTypeAction, mixinTypeSpec);
    }

    static Function<ObjectActionDefault, ObjectAssociation> mixedInAssociation(
            final ObjectSpecification mixedInType,
            final Class<?> mixinType,
            final String mixinMethodName) {

        return mixinAction -> {
            val returnType = mixinAction.getReturnType();
            if (returnType.isNotCollection()) {
                return new OneToOneAssociationMixedIn(
                        mixinAction, mixedInType, mixinType, mixinMethodName);
            }
            return new OneToManyAssociationMixedIn(
                    mixinAction, mixedInType, mixinType, mixinMethodName);
        };
    }





}
