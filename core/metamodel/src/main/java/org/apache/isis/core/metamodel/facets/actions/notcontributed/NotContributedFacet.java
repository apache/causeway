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

package org.apache.isis.core.metamodel.facets.actions.notcontributed;

import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.val;

/**
 * Indicates that the action should not be contributed to any
 * objects.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating the
 * action method using {@code @Programmatic}.
 */
public interface NotContributedFacet extends Facet {

    public Contributed contributed();

    default boolean isActionContributionVetoed() {
        // not contributed to actions if...
        return contributed() == Contributed.AS_NEITHER 
                || contributed() == Contributed.AS_ASSOCIATION;
    }
    
    default boolean isAssociationContributionVetoed() {
        // not contributed to associations if...
        return contributed() == Contributed.AS_NEITHER 
                || contributed() == Contributed.AS_ACTION;
    }
    
    // -- UTILITIES
    
    static boolean isActionContributionVetoed(final ObjectAction action) {
        val notContributed = action.getFacet(NotContributedFacet.class);
        if(notContributed != null 
                && notContributed.isActionContributionVetoed()) {
            return true;
        }
        return false;
    }
    
    static boolean isAssociationContributionVetoed(final ObjectAction action) {
        val notContributed = action.getFacet(NotContributedFacet.class);
        if(notContributed != null 
                && notContributed.isAssociationContributionVetoed()) {
            return true;
        }
        return false;
    }

}
