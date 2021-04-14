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
package org.apache.isis.core.metamodel.specloader.specimpl;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.contributing.ContributingFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.val;

/** package private utility */
final class Predicates {
    
    // -- LOWER LEVEL
    
    static boolean isRequiredType(final ObjectAction objectAction) {
        return objectAction instanceof ObjectActionDefault;
    }
    
    static boolean isGetterCandidate(final ObjectAction action) {
        if(action.getParameterCount() != 0) {
            return false;
        }
        if(!action.hasReturn()) {
            return false;
        }
        return true;
    }
    
    static boolean isAlwaysHidden(final FacetHolder holder) {
        val hiddenFacet = holder.getFacet(HiddenFacet.class);
        return hiddenFacet != null && hiddenFacet.where() == Where.ANYWHERE;
    }
    
    // -- HIGHER LEVEL - MIXINS
    
    static boolean isMixedInAction(ObjectAction mixinTypeAction) {
        if(!isRequiredType(mixinTypeAction)) {
            return false;
        }
        if(isAlwaysHidden(mixinTypeAction)) {
            return false;
        }
        if(ContributingFacet.isActionContributionVetoed(mixinTypeAction)) {
            return false;
        }
        return true;
    }
    
    static boolean isMixedInAssociation(ObjectAction mixinAction) {
        if(!isRequiredType(mixinAction)) {
            return false;
        }
        if(isAlwaysHidden(mixinAction)) {
            return false;
        }
        if(ContributingFacet.isAssociationContributionVetoed(mixinAction)) {
            return false;
        }
        if(!isGetterCandidate(mixinAction)) {
            return false;
        }
        if(!mixinAction.getSemantics().isSafeInNature()) {
            return false;
        }
        return true;
        
    }
    
    

}
