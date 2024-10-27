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

import java.util.Comparator;
import java.util.Objects;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Objects;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MmSortUtils {

    public enum SortDirection {
        ASCENDING,
        DESCENDING;
    }

    public Comparator<ManagedObject> orderingBy(final ObjectAssociation sortProperty, final SortDirection sortDirection) {
        final Comparator<ManagedObject> comparator = (sortDirection != SortDirection.DESCENDING)
                ? NATURAL_NULL_FIRST
                : NATURAL_NULL_FIRST.reversed();

        return (p, q) -> {
            var pSort = sortProperty.get(p, InteractionInitiatedBy.PASS_THROUGH);
            var qSort = sortProperty.get(q, InteractionInitiatedBy.PASS_THROUGH);
            return comparator.compare(pSort, qSort);
        };
    }

    public int compare(final @Nullable ManagedObject p, final @Nullable ManagedObject q) {
        return NATURAL_NULL_FIRST.compare(p, q);
    }

    // -- PREDEFINED COMPARATOR

    final Comparator<ManagedObject> NATURAL_NULL_FIRST = (a, b) -> {

        var aPojo = MmUnwrapUtils.single(a);
        var bPojo = MmUnwrapUtils.single(b);
        if(Objects.equals(aPojo, bPojo)) {
            return 0;
        }
        if((aPojo==null
                || aPojo instanceof Comparable)
            && (bPojo==null
                    || bPojo instanceof Comparable)) {
            return _Objects.compareNullsFirst((Comparable)aPojo, (Comparable)bPojo);
        }
        final int hashCompare = Integer.compare(Objects.hashCode(aPojo), Objects.hashCode(bPojo));
        if(hashCompare!=0) {
            return hashCompare;
        }
        //XXX on hash-collision we return an arbitrary non-equal relation (unspecified behavior)
        return -1;
    };

}
