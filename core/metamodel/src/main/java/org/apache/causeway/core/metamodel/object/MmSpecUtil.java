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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.NonNull;
import lombok.val;
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

    /**
     * Introduced for JUnit testing.
     * <p>
     * Shortcut for {@code specificationsBySortAsYaml(specLoader.snapshotSpecifications())}.
     */
    public String specificationsBySortAsYaml(final @NonNull SpecificationLoader specLoader) {
        return specificationsBySortAsYaml(specLoader.snapshotSpecifications());
    }

    /**
     * Introduced for JUnit testing.
     */
    public String specificationsBySortAsYaml(final @NonNull Can<ObjectSpecification> specs) {

        // collect all ObjectSpecifications into a list-multi-map, where BeanSort is the key
        var specsBySort = _Multimaps.<String, String>newListMultimap(LinkedHashMap<String, List<String>>::new, ArrayList::new);
        specs
                .stream()
                .sorted()
                .forEach(spec->specsBySort.putElement(spec.getBeanSort().name(), spec.getLogicalTypeName()));

        // export the list-multi-map to YAML format
        val sb = new StringBuilder();
        sb.append("ObjectSpecifications:\n");
        specsBySort
            .forEach((key, list)->{
                sb.append(String.format("  %s:\n", key));
                list.forEach(logicalTypeName->{
                    sb.append(String.format("  - %s\n", logicalTypeName));
                });
            });

        return sb.toString();
    }

}