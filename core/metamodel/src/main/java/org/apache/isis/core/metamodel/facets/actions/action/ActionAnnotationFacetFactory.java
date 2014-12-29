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

package org.apache.isis.core.metamodel.facets.actions.action;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class ActionAnnotationFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware {

    private ServicesInjector servicesInjector;

    public ActionAnnotationFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        if (action == null) {
            return;
        }

//        processInteraction(processMethodContext);
    }

//    private void processInteraction(final ProcessMethodContext processMethodContext) {
//        final Method method = processMethodContext.getMethod();
//        final Property property = Annotations.getAnnotation(method, Property.class);
//        final FacetHolder holder = processMethodContext.getFacetHolder();
//
//        FacetUtil.addFacet(
//                PropertyInteractionFacetForPropertyAnnotation.create(
//                        property, servicesInjector, getSpecificationLoader(), holder));
//    }


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }
}
