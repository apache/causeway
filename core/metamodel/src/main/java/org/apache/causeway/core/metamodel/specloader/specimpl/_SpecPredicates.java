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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import org.apache.causeway.core.metamodel.facets.actions.contributing.ContributingFacet;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.NonNull;

/** package private utility */
final class _SpecPredicates {

    // -- LOWER LEVEL

    static boolean isGetterCandidate(final @NonNull ObjectAction action) {
        if(action.getParameterCount() != 0) {
            return false;
        }
        if(!action.hasReturn()) {
            return false;
        }
        return true;
    }

    // -- HIGHER LEVEL - MIXINS

    static boolean isMixedInAction(final @NonNull ObjectAction mixinTypeAction) {
        if(HiddenFacet.isAlwaysHidden(mixinTypeAction)) {
            return false;
        }
        if(ContributingFacet.isActionContributionVetoed(mixinTypeAction)) {
            return false;
        }
        return true;
    }

    static boolean isMixedInAssociation(final @NonNull ObjectAction mixinAction) {
        if(!isGetterCandidate(mixinAction)) {
            return false;
        }
        if(!mixinAction.getSemantics().isSafeInNature()) {
            return false;
        }
        if(HiddenFacet.isAlwaysHidden(mixinAction)) {
            return false;
        }
        if(ContributingFacet.isAssociationContributionVetoed(mixinAction)) {
            return false;
        }
        return true;
    }



}
