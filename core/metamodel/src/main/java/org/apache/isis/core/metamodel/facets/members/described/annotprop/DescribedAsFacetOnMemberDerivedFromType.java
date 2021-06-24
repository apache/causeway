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

package org.apache.isis.core.metamodel.facets.members.described.annotprop;

import java.util.Optional;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.described.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.described.DescribedAsFacetAbstract;

public class DescribedAsFacetOnMemberDerivedFromType
extends DescribedAsFacetAbstract {

    /**
     * As {@link DescribedAsFacet}(s) have either static or dynamic (imperative) text,
     * we yet only support inferring from those with static text.
     */
    public static Optional<DescribedAsFacet> create(
            final DescribedAsFacet describedAsFacet,
            final FacetHolder holder) {

        return describedAsFacet instanceof DescribedAsFacetAbstract
                ? Optional.of(
                        new DescribedAsFacetOnMemberDerivedFromType(
                                (DescribedAsFacetAbstract) describedAsFacet,
                                holder))
                : Optional.empty();
    }

    private DescribedAsFacetOnMemberDerivedFromType(
            final DescribedAsFacetAbstract describedAsFacet,
            final FacetHolder holder) {
        super(describedAsFacet.text(), holder);
    }

}
