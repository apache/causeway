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

package org.apache.isis.core.metamodel.facets;

import java.util.List;
import java.util.Properties;

import com.google.common.collect.Lists;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.lang.PropertiesExtensions;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.ContributeeMember;

/**
 * A {@link FacetFactory} which is applied to only for {@link ContributeeMember}s.
 */
public interface ContributeeMemberFacetFactory extends FacetFactory {

    public static class ProcessContributeeMemberContext extends AbstractProcessContext<ObjectMember> 
            implements ProcessContextWithMetadataProperties<ObjectMember> {
        
        private final Properties metadataProperties;

        public ProcessContributeeMemberContext(
                final Properties metadataProperties, 
                final ObjectMember facetHolder) {
            super(facetHolder);
            this.metadataProperties = metadataProperties;
        }

        public Properties metadataProperties(String subKey) {
            
            if(metadataProperties == null) {
                return null;
            }
            Identifier identifier = getFacetHolder().getIdentifier();
            final String id = identifier.getMemberName();
            
            // build list of keys to search for... 
            final List<String> keys = Lists.newArrayList();
            if(getFacetHolder() instanceof ObjectAction) {
                // ... either "action.actionId" or "member.actionId()" 
                keys.add("action." + id+"."+subKey);
                keys.add("member." + id+"()"+"."+subKey);
            } else if(getFacetHolder() instanceof OneToOneAssociation) {
                // ... either "property.propertyId" or "member.propertyId"  
                keys.add("property." + id+"."+subKey);
                keys.add("member." + id+"."+subKey);
            } else if(getFacetHolder() instanceof OneToManyAssociation) {
                // ... either "collection.collectionId" or "member.collectionId" 
                keys.add("member." + id+"."+subKey);
                keys.add("collection." + id+"."+subKey);
            }

            for (final String key : keys) {
                final Properties subsetProperties = PropertiesExtensions.subset(this.metadataProperties, key);
                if (!subsetProperties.isEmpty()) {
                    return subsetProperties;
                } 
            }
            
            return null;
        }
    }
    
    /**
     * Sort the member, and return the correctly setup annotation if present.
     */
    void process(ProcessContributeeMemberContext processMemberContext);

}
