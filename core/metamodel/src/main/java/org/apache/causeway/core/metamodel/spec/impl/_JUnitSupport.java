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
package org.apache.causeway.core.metamodel.spec.impl;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.beans.CausewayBeanTypeClassifier;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.experimental.UtilityClass;

@UtilityClass
public class _JUnitSupport {

    public SpecificationLoader specLoader(
            final CausewayConfiguration causewayConfiguration,
            final CausewaySystemEnvironment causewaySystemEnvironment,
            final ServiceRegistry serviceRegistry,
            final ProgrammingModel programmingModel,
            final CausewayBeanTypeClassifier causewayBeanTypeClassifier,
            final CausewayBeanTypeRegistry causewayBeanTypeRegistry,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {

        return SpecificationLoaderDefault
                .instanceForTesting(causewayConfiguration, causewaySystemEnvironment, serviceRegistry,
                        programmingModel, causewayBeanTypeClassifier, causewayBeanTypeRegistry,
                        classSubstitutorRegistry);
    }

    public OneToOneAssociationMixedIn mixedInProp(
            final ObjectSpecification mixeeSpec,
            final ObjectSpecification mixinSpec,
            final String mixinMethodName,
            final FacetedMethod facetedMethod) {
        final ObjectActionDefault mixinAction = (ObjectActionDefault) actionForMixinMain(facetedMethod);
        return new OneToOneAssociationMixedIn(mixeeSpec, mixinAction, mixinSpec, mixinMethodName);
    }

    public OneToManyAssociation mixedInColl(
            final ObjectSpecification mixeeSpec,
            final ObjectSpecification mixinSpec,
            final String mixinMethodName,
            final FacetedMethod facetedMethod) {
        final ObjectActionDefault mixinAction = (ObjectActionDefault) actionForMixinMain(facetedMethod);
        return new OneToManyAssociationMixedIn(mixeeSpec, mixinAction, mixinSpec, mixinMethodName);
    }

    public ObjectAction actionForMethod(
            final FacetedMethod facetedMethod) {
        return new ObjectActionDefault(facetedMethod.getFeatureIdentifier(), facetedMethod, false, true);
    }
    public ObjectAction actionForMixinMain(final FacetedMethod facetedMethod) {
        return new ObjectActionDefault(facetedMethod.getFeatureIdentifier(), facetedMethod, true, true);
    }
    public ObjectActionMixedIn mixedInActionforMixinMain(
            final ObjectSpecification mixeeSpec,
            final ObjectSpecification mixinSpec,
            final String mixinMethodName,
            final FacetedMethod facetedMethod) {
        final ObjectActionDefault mixinAction = (ObjectActionDefault) actionForMixinMain(facetedMethod);
        return new ObjectActionMixedIn(mixinSpec, mixinMethodName, mixinAction, mixeeSpec);
    }

}
