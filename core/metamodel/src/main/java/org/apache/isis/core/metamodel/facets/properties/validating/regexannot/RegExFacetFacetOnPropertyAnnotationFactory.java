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

package org.apache.isis.core.metamodel.facets.properties.validating.regexannot;

import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.object.regex.TitleFacetFormattedByRegex;

public class RegExFacetFacetOnPropertyAnnotationFactory extends FacetFactoryAbstract {

    public RegExFacetFacetOnPropertyAnnotationFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Class<?> returnType = processMethodContext.getMethod().getReturnType();
        if (!Annotations.isString(returnType)) {
            return;
        }
        final RegEx annotation = Annotations.getAnnotation(processMethodContext.getMethod(), RegEx.class);
        if(annotation == null) {
            return;
        }
        addRegexFacetAndCorrespondingTitleFacet(processMethodContext.getFacetHolder(), annotation);
    }

    private void addRegexFacetAndCorrespondingTitleFacet(final FacetHolder holder, final RegEx annotation) {
        final RegExFacet regexFacet = RegExFacetOnPropertyAnnotation.createRegexFacet(annotation, holder);
        FacetUtil.addFacet(regexFacet);

        final TitleFacet titleFacet = createTitleFacet(regexFacet);
        FacetUtil.addFacet(titleFacet);
    }

    private TitleFacet createTitleFacet(final RegExFacet regexFacet) {
        return new TitleFacetFormattedByRegex(regexFacet);
    }

}
