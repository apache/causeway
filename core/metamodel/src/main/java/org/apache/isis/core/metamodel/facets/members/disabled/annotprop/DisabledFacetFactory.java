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

package org.apache.isis.core.metamodel.facets.members.disabled.annotprop;

import java.util.Properties;

import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;

public class DisabledFacetFactory extends FacetFactoryAbstract 
        implements ContributeeMemberFacetFactory {

    public DisabledFacetFactory() {
        super(FeatureType.MEMBERS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        DisabledFacet disabledFacet = createFromMetadataPropertiesIfPossible(processMethodContext);
        if(disabledFacet == null) {
            disabledFacet = createFromAnnotationIfPossible(processMethodContext);
        }
        // no-op if null
        FacetUtil.addFacet(disabledFacet);
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {
        DisabledFacet disabledFacet = createFromMetadataPropertiesIfPossible(processMemberContext);
        // no-op if null
        FacetUtil.addFacet(disabledFacet);
    }

    private static DisabledFacet createFromAnnotationIfPossible(final ProcessMethodContext processMethodContext) {
        final Disabled annotation = Annotations.getAnnotation(processMethodContext.getMethod(), Disabled.class);
        return annotation != null 
                ? new DisabledFacetAnnotation(annotation.when(), annotation.where(), annotation.reason(), processMethodContext.getFacetHolder()) 
                : null;
    }

    private static DisabledFacet createFromMetadataPropertiesIfPossible(
            final ProcessContextWithMetadataProperties<? extends FacetHolder> pcwmp) {
        
        final FacetHolder holder = pcwmp.getFacetHolder();
        
        final Properties properties = pcwmp.metadataProperties("disabled");
        return properties != null ? new DisabledFacetFromProperties(properties, holder) : null;
    }


}
