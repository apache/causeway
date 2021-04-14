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

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.NonNull;
import lombok.Value;

/**
 *
 * @since 2.0
 *
 */
@Value(staticConstructor = "of")
public final class ValidationFailure implements Comparable<ValidationFailure> {

    @NonNull private Identifier origin;
    @NonNull private String message;

    // -- FACTORIES
    
    /**
     * Collects a new ValidationFailure with given origin and message. 
     */
    public static void raise(
            @NonNull SpecificationLoader specLoader,
            @NonNull Identifier deficiencyOrigin, 
            @NonNull String deficiencyMessage) {
        
        specLoader.addValidationFailure(ValidationFailure.of(deficiencyOrigin, deficiencyMessage));
    }
    
    /**
     * Collects a new ValidationFailure for given FacetHolder (that is the origin) using given message. 
     */
    public static void raise(
            @NonNull IdentifiedHolder facetHolder, 
            @NonNull String deficiencyMessage) {
        raise(facetHolder.getSpecificationLoader(), facetHolder.getIdentifier(), deficiencyMessage);
    }
    
    /**
     * Collects a new ValidationFailure for given FacetHolder (that is the origin) using given message
     * (assembled from format and args). 
     */
    public static void raiseFormatted(
            @NonNull IdentifiedHolder facetHolder, 
            @NonNull String messageFormat, 
            final Object ...args) {
        raise(facetHolder, String.format(messageFormat, args));
    }
    
    
    private static final Comparator<ValidationFailure> comparator = Comparator
            .<ValidationFailure, String>comparing(failure->failure.getOrigin().getClassName(), nullsFirst(naturalOrder()))
            .<String>thenComparing(failure->failure.getOrigin().getMemberName(), nullsFirst(naturalOrder()))
            .thenComparing(ValidationFailure::getMessage);

    // -- CONTRACT
    
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
