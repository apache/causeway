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
package org.apache.isis.core.metamodel.specloader.validator;

import java.util.Comparator;
import java.util.Objects;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import org.apache.isis.applib.id.FeatureIdentifier;

import lombok.NonNull;
import lombok.Value;

/**
 *
 * @since 2.0
 *
 */
@Value(staticConstructor = "of")
public final class ValidationFailure implements Comparable<ValidationFailure> {

    @NonNull private FeatureIdentifier origin;
    @NonNull private String message;

    private static final Comparator<ValidationFailure> comparator = Comparator
            .<ValidationFailure, String>comparing(failure->failure.getOrigin().getClassName(), nullsFirst(naturalOrder()))
            .<String>thenComparing(failure->failure.getOrigin().getMemberName(), nullsFirst(naturalOrder()))
            .thenComparing(ValidationFailure::getMessage);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationFailure that = (ValidationFailure) o;
        return message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public int compareTo(ValidationFailure o) {

        if(equals(o)) {
            return 0; // for consistency with equals
        }

        if(o==null) {
            return -1; // null last policy
        }

        return comparator.compare(this, o);
    }

}
