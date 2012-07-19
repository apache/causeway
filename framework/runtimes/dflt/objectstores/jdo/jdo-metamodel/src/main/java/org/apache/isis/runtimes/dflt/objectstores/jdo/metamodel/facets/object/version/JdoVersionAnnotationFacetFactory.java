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
package org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.version;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Version;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

public class JdoVersionAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public JdoVersionAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_POST_PROCESSING_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        final Version annotation = getAnnotation(processClassContext.getCls(), Version.class);
        if (annotation == null) {
            return;
        }
        final ObjectSpecification objSpec = (ObjectSpecification) processClassContext.getFacetHolder();
        String propertyId = getPropertyId(annotation);
        ObjectAssociation otoa = objSpec.getAssociation(propertyId);
        if (otoa == null) {
            throw new RuntimeException("No such property '" + propertyId + "'");
        }

        FacetUtil.addFacet(new JdoVersionFacetAnnotation(otoa));
        FacetUtil.addFacet(new DisabledFacetDerivedFromJdoVersionAnnotation(otoa));
        FacetUtil.addFacet(new OptionalFacetDerivedFromJdoVersionAnnotation(otoa));
    }

    private static String getPropertyId(final Version annotation) {
        final Extension[] extensions = annotation.extensions();
        for(Extension extension: extensions) {
            if("datanucleus".equals(extension.vendorName()) && "field-name".equals(extension.key())) {
                return  extension.value();
            }
        }
        return annotation.column();
    }

}
