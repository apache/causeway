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
package org.apache.causeway.core.metamodel.object;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MmSpecUtil {

    /**
     * optimized for the case when a specification that probably matches is known in advance
     * the result must be an instance of guess
     * @throws AssertionError if guess is not assignable from actual type
     */
    public ObjectSpecification quicklyResolveObjectSpecification(
            final @NonNull ObjectSpecification guess,
            final @NonNull Class<?> requiredType) {
        return guess.getCorrespondingClass().equals(requiredType)
                // when successful guess
                ? guess
                // else lookup
                : MmAssertionUtil.assertTypeOf(guess)
                    .apply(guess.getSpecificationLoader().specForTypeElseFail(requiredType));
    }

}