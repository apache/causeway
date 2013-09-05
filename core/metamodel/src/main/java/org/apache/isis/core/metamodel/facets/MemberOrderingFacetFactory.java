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

import java.util.Properties;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.lang.PropertyUtil;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

/**
 * A {@link FacetFactory} which orders {@link ObjectMember}s (eg, according to
 * the {@link MemberOrderFacet}).
 */
public interface MemberOrderingFacetFactory extends FacetFactory {

    public static class ProcessMemberContext extends AbstractProcessContext<ObjectMember> implements ProcessContextWithMetadataProperties<ObjectMember> {
        
        private final Properties metadataProperties;

        public ProcessMemberContext(
                final Properties metadataProperties, 
                final ObjectMember facetHolder) {
            super(facetHolder);
            this.metadataProperties = metadataProperties;
        }

        public Properties metadataProperties(String prefix) {
            
            if(metadataProperties == null) {
                return null;
            }
            Identifier identifier = getFacetHolder().getIdentifier();
            final String id = identifier.getMemberName();
            
            // bit of a hack; to distinguish between actions and properties that have same identifier
            // eg getPaidBy() and paidBy()
            if(getFacetHolder() instanceof ObjectAction) {
                Properties subsetProperties = PropertyUtil.subset(this.metadataProperties, prefix+"."+id+"()");
                if (!subsetProperties.isEmpty()) {
                    return subsetProperties;
                } 
            }

            // otherwise, regular processing...
            Properties subsetProperties = PropertyUtil.subset(this.metadataProperties, prefix+"."+id);
            if (!subsetProperties.isEmpty()) {
                return subsetProperties;
            }
            
            return null;
        }
    }
    
    /**
     * Sort the member, and return the correctly setup annotation if present.
     */
    void process(ProcessMemberContext processMemberContext);

}
