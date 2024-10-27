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
package org.apache.causeway.core.metamodel.facets.properties.property.mandatory.jaxb;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlElement;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;

public class MandatoryFacetFromXmlElementAnnotationFactory
extends FacetFactoryAbstract {

    @Inject
    public MandatoryFacetFromXmlElementAnnotationFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        if(processMethodContext.isMixinMain()) {
            return; // shortcut just in case
        }

        var xmlElementIfAny = processMethodContext.synthesizeOnMethod(XmlElement.class);
        var facetHolder = processMethodContext.getFacetHolder();

        // search for @XmlElement(required=...)
        addFacetIfPresent(
                MandatoryFacetFromXmlElementAnnotation.create(xmlElementIfAny, facetHolder));
    }

}
