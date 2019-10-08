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
package org.apache.isis.metamodel.specloader.specimpl;

import java.util.function.Predicate;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.actions.notcontributed.NotContributedFacet;
import org.apache.isis.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;

import lombok.val;

/** package private utility */
final class Predicates {
    
    // -- LOWER LEVEL
    
    static boolean isRequiredType(final ObjectAction objectAction) {
        return objectAction instanceof ObjectActionDefault;
    }
    
    static boolean isActionContributionVetoed(final ObjectAction contributeeAction) {
        val notContributed = contributeeAction.getFacet(NotContributedFacet.class);
        if(notContributed != null && notContributed.toActions()) {
            return true;
        }
        return false;
    }
    
    static boolean isAlwaysHidden(final FacetHolder holder) {
        val hiddenFacet = holder.getFacet(HiddenFacet.class);
        return hiddenFacet != null && hiddenFacet.where() == Where.ANYWHERE;
    }
    
    static boolean isAnyContributeeParameterMatching(
            final ObjectSpecification typeSpec, 
            final ObjectAction contributeeAction) {

        return Utils.contributeeParameterIndexOf(typeSpec, contributeeAction)!=-1;
    }
    
    // -- HIGHER LEVEL
    
    static Predicate<ObjectAction> isContributeeAssociation(
            final ObjectSpecification typeSpec) {
        
        return contributeeAction -> {
            if(!isRequiredType(contributeeAction)) {
                return false;
            }
            if(isAlwaysHidden(contributeeAction)) {
                return false;
            }
            if(isActionContributionVetoed(contributeeAction)) {
                return false;
            }
            if(!contributeeAction.hasReturn()) {
                return false;
            }
            if(contributeeAction.getParameterCount() != 1) {
                return false;
            }
            if(!contributeeAction.getSemantics().isSafeInNature()) {
                return false;
            }
            if(!isAnyContributeeParameterMatching(typeSpec, contributeeAction)) {
                return false;
            }
            return true;
        };
    }
    
    static Predicate<ObjectAction> isContributeeAction(
            final ObjectSpecification typeSpec) {
        
        return contributeeAction -> {
        
            if(!isRequiredType(contributeeAction)) {
                return false;
            }
            if(isAlwaysHidden(contributeeAction)) {
                return false;
            }
            if(isActionContributionVetoed(contributeeAction)) {
                return false;
            }
            if(!isAnyContributeeParameterMatching(typeSpec, contributeeAction)) {
                return false;
            }
            return true; 
        };
    }
    
    static boolean isMixedInAction(ObjectAction mixinTypeAction) {
        if(!isRequiredType(mixinTypeAction)) {
            return false;
        }
        if(isAlwaysHidden(mixinTypeAction)) {
            return false;
        }
        if(isActionContributionVetoed(mixinTypeAction)) {
            return false;
        }
        return true;
    }
    
    static boolean isMixedInAssociation(ObjectAction mixinAction) {
        if(!isRequiredType(mixinAction)) {
            return false;
        }
        if(isActionContributionVetoed(mixinAction)) {
            return false;
        }
        if(mixinAction.getParameterCount() != 0) {
            return false;
        }
        if(!mixinAction.getSemantics().isSafeInNature()) {
            return false;
        }
        return true;
        
    }
    
    

}
