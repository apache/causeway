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

import java.util.Optional;

import javax.xml.bind.annotation.XmlElement;

import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacetAbstract;

/**
 * Inferred from presence of an {@code @XmlElement(required = false/true)} annotation.
 *
 * @implNote There is an ambiguous use case {@code @XmlElement(nillable = true, required = true)},
 * where at the time of writing we were not sure whether this maps onto
 * {@link org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet.Semantics#OPTIONAL OPTIONAL}
 * or
 * {@link org.apache.causeway.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet.Semantics#REQUIRED REQUIRED}.
 * <p>
 * Ignoring the nillable attribute completely for now. In the future, if we want to refine things,
 * then we would do that by figuring out what "nillable" actually means in our meta-model
 * (perhaps not the same as mandatory/optional).
 */
public class MandatoryFacetFromXmlElementAnnotation
extends MandatoryFacetAbstract {

    public static Optional<MandatoryFacetAbstract> create(
            final Optional<XmlElement> xmlElementIfAny, final FacetHolder facetHolder){
        return xmlElementIfAny
                .map(XmlElement::required)
                .map(Semantics::required)
                .map(semantics->new MandatoryFacetFromXmlElementAnnotation(semantics, facetHolder));
    }

    private MandatoryFacetFromXmlElementAnnotation(
            final Semantics semantics, final FacetHolder holder) {
        super(semantics, holder);
    }

}

