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

package org.apache.isis.core.metamodel.facets.properties.property;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class PropertyAnnotationFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware {

    private ServicesInjector servicesInjector;

    public PropertyAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        if (property == null) {
            return;
        }

        processInteraction(processMethodContext);
        processDisabled(processMethodContext);
        processMaxLength(processMethodContext);
        processMustSatisfy(processMethodContext);
        processNotPersisted(processMethodContext);
        processOptional(processMethodContext);
        processRegEx(processMethodContext);
    }

    private void processInteraction(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                PropertyInteractionFacetForPropertyAnnotation.create(
                        property, servicesInjector, getSpecificationLoader(), holder));
    }

    private void processDisabled(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                DisabledFacetForPropertyAnnotation.create(property, holder));
    }

    private void processMaxLength(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                MaxLengthFacetForPropertyAnnotation.create(property, holder));
    }

    private void processMustSatisfy(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                MustSatisfySpecificationFacetForPropertyAnnotation.create(property, holder));
    }

    private void processNotPersisted(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                NotPersistedFacetForPropertyAnnotation.create(property, holder));
    }

    private void processOptional(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(MandatoryFacetForPropertyAnnotation.create(property, method, holder));
    }

    private void processRegEx(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Property property = Annotations.getAnnotation(method, Property.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(RegExFacetForPropertyAnnotation.create(property, holder));
    }


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }
}
