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
package org.apache.causeway.core.metamodel.facets.actions.contributing;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet.Contributing;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

/**
 * Indicates that the action should be contributed to objects either
 * as <i>Action</i>, <i>Property</i> or <i>Collection</i>.
 * <p>
 * Since v2 only ever used for mixed in actions.
 * @since 2.0
 */
public interface ContributingFacet extends Facet {

    public Contributing contributed();

    default boolean isActionContributionVetoed() {
        return contributed() == Contributing.AS_PROPERTY
                || contributed() == Contributing.AS_COLLECTION;
    }

    default boolean isAssociationContributionVetoed() {
        return contributed() == Contributing.AS_ACTION;
    }

    // -- UTILITIES

    static boolean isActionContributionVetoed(final ObjectAction action) {
        var facet = action.getFacet(ContributingFacet.class);
        if(facet != null) {
            return facet.isActionContributionVetoed();
        }
        return false;
    }

    static boolean isAssociationContributionVetoed(final ObjectAction action) {
        var facet = action.getFacet(ContributingFacet.class);
        if(facet != null) {
            return facet.isAssociationContributionVetoed();
        }
        return false;
    }

}
