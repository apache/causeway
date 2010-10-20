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


package org.apache.isis.extensions.wicket.metamodel.facets;


import java.lang.reflect.Method;

import org.apache.isis.extensions.wicket.applib.WizardPageDescription;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.FacetUtil;
import org.apache.isis.metamodel.facets.MethodRemover;
import org.apache.isis.metamodel.java5.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.spec.feature.ObjectFeatureType;


public class WizardPageDescriptionAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract {

    public WizardPageDescriptionAnnotationFacetFactory() {
        super(ObjectFeatureType.PROPERTIES_ONLY);
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {

        // look for annotation on the property
        final WizardPageDescription annotation = getAnnotation(method, WizardPageDescription.class);
        WizardPageDescriptionFacet facet = create(annotation, holder);
        if (facet != null) {
            return FacetUtil.addFacet(facet);
        }

        return false;
    }

    private WizardPageDescriptionFacet create(final WizardPageDescription annotation, final FacetHolder holder) {
        return annotation == null ? null : new WizardPageDescriptionFacetAnnotation(holder);
    }

}
