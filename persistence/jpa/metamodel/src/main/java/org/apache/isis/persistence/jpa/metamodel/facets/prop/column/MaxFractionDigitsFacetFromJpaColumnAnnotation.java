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
package org.apache.isis.persistence.jpa.metamodel.facets.prop.column;

import java.util.Optional;

import javax.persistence.Column;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacetAbstract;

public class MaxFractionDigitsFacetFromJpaColumnAnnotation
extends MaxFractionalDigitsFacetAbstract {

     public static Optional<MaxFractionalDigitsFacet> create(
             final Optional<Column> jpaColumnIfAny,
             final FacetHolder holder) {

         return jpaColumnIfAny
         .filter(jpaColumn->jpaColumn.scale()>=0)
         .map(jdoColumn->{
             return new MaxFractionDigitsFacetFromJpaColumnAnnotation(
                     jdoColumn.scale(), holder);
         });
    }

    private MaxFractionDigitsFacetFromJpaColumnAnnotation(
            final int maxFractionalDigits, final FacetHolder holder) {
        super(maxFractionalDigits, holder);
    }

}
