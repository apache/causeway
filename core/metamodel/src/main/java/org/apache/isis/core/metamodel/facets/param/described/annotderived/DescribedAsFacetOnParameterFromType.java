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
package org.apache.isis.core.metamodel.facets.param.described.annotderived;

import java.util.Optional;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.described.ParamDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.described.ParamDescribedFacetAbstract;

import lombok.val;

public class DescribedAsFacetOnParameterFromType
extends ParamDescribedFacetAbstract {

    public static Optional<ParamDescribedFacet> create(
            final ObjectDescribedFacet objectDescribedFacet,
            final FacetHolder holder) {

        val describedIfAny = _Strings.emptyToNull(objectDescribedFacet.text());

        return Optional.ofNullable(describedIfAny)
        .map(described->
            new DescribedAsFacetOnParameterFromType(described, holder));
    }

    private DescribedAsFacetOnParameterFromType(
            final String described,
            final FacetHolder holder) {
        super(described, holder, Precedence.INFERRED);
    }

}
