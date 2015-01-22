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

package org.apache.isis.core.metamodel.facets.properties.multiline.annotation;

import java.util.Properties;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.propparam.labelat.LabelAtFacetInferredFromMultiLineFacet;
import org.apache.isis.core.metamodel.facets.propparam.multiline.MultiLineFacet;

public class MultiLineFacetOnPropertyFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory {

    public MultiLineFacetOnPropertyFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        MultiLineFacet facet = createFromMetadataPropertiesIfPossible(processMethodContext);
        if(facet == null) {
            facet = createFromAnnotationIfPossible(processMethodContext);
        }
        
        // no-op if null
        FacetUtil.addFacet(facet);

        // no-op if null
        inferPropParamLayoutFacet(facet);
    }

    private static void inferPropParamLayoutFacet(MultiLineFacet facet) {
        if (facet == null) {
            return;
        }
        FacetUtil.addFacet(new LabelAtFacetInferredFromMultiLineFacet(facet.getFacetHolder()));
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {
        
    }
    
    private static MultiLineFacet createFromMetadataPropertiesIfPossible(
            final ProcessContextWithMetadataProperties<? extends FacetHolder> pcwmp) {
        
        final FacetHolder holder = pcwmp.getFacetHolder();
        
        final Properties properties = pcwmp.metadataProperties("multiLine");
        return properties != null ? new MultiLineFacetOnPropertyFromProperties(properties, holder) : null;
    }

    private static MultiLineFacetOnPropertyAnnotation createFromAnnotationIfPossible(final ProcessMethodContext processMethodContext) {
        final Class<?> returnType = processMethodContext.getMethod().getReturnType();
        if (!Annotations.isString(returnType)) {
            return null;
        }
        final MultiLine annotation = Annotations.getAnnotation(processMethodContext.getMethod(), MultiLine.class);
        return (annotation != null) ? new MultiLineFacetOnPropertyAnnotation(annotation.numberOfLines(), annotation.preventWrapping(), processMethodContext.getFacetHolder()) : null;
    }


}
