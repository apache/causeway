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

package org.apache.isis.core.metamodel.facets.object.logicaltype.classname;

import java.util.Collections;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.logicaltype.LogicalTypeFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;

public class LogicalTypeFacetDerivedFromClassNameFactory
extends FacetFactoryAbstract
implements
        ObjectTypeFacetFactory,
        MetaModelRefiner {

    private ClassSubstitutorRegistry classSubstitutorRegistry;

    @Inject
    public LogicalTypeFacetDerivedFromClassNameFactory(
            final MetaModelContext mmc,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {
        super(mmc, FeatureType.OBJECTS_ONLY);
        this.classSubstitutorRegistry = classSubstitutorRegistry;
    }

    // -- JUNIT SUPPORT

    public static LogicalTypeFacetDerivedFromClassNameFactory forTesting(
            final MetaModelContext mmc) {
        return new LogicalTypeFacetDerivedFromClassNameFactory(
                mmc,
                new ClassSubstitutorRegistry(Collections.singletonList(new ClassSubstitutorDefault())));
    }

    @Override
    public void process(final ProcessObjectTypeContext processClassContext) {
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        // don't trash existing facet
        if(facetHolder.containsNonFallbackFacet(LogicalTypeFacet.class)) {
            return;
        }
        val cls = processClassContext.getCls();
        val substitute = classSubstitutorRegistry.getSubstitution(cls);
        if(substitute.isNeverIntrospect()) {
            return;
        }
        val logicalTypeFacet = createLogicalTypeFacet(facetHolder, substitute.apply(cls));
        FacetUtil.addFacetIfPresent(logicalTypeFacet);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        // now a no-op.
    }

    private static LogicalTypeFacet createLogicalTypeFacet(
            final FacetHolder facetHolder,
            final Class<?> substitutedClass) {

        val serviceId = getServiceId(facetHolder);
        val isService = serviceId!=null;

        if (isService) {
            return new LogicalTypeFacetInferredFromIoCNamingStrategy(
                    LogicalType.eager(substitutedClass, serviceId),
                    facetHolder);
        }
        return new LogicalTypeFacetInferredFromClassName(substitutedClass, facetHolder);
    }

    private static String getServiceId(final FacetHolder facetHolder) {
        if(facetHolder instanceof ObjectSpecification) {
            ObjectSpecification objectSpecification = (ObjectSpecification) facetHolder;
            if(objectSpecification.isManagedBean()) {
                return objectSpecification.getManagedBeanName();
            }
        }
        return null;
    }

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {

        val shouldCheck = getConfiguration().getCore().getMetaModel().getValidator().isExplicitLogicalTypeNames();
        if(!shouldCheck) {
            return;
        }

        programmingModel.addVisitingValidatorSkipManagedBeans(objectSpec-> {

            if(!check(objectSpec)) {
                return;
            }

            val logicalTypeFacet = objectSpec.getFacet(LogicalTypeFacet.class);
            if(logicalTypeFacet instanceof LogicalTypeFacetInferredFromClassName) {
                ValidationFailure.raiseFormatted(
                        objectSpec,
                        "%s: the object type must be specified explicitly ('%s' config property). "
                                + "Defaulting the object type from the package/class/package name can lead "
                                + "to data migration issues for apps deployed to production (if the class is "
                                + "subsequently refactored). "
                                + "Use @Discriminator, @DomainObject(logicalTypeName=...) or "
                                + "@PersistenceCapable(schema=...) to specify explicitly.",
                        objectSpec.getFullIdentifier(),
                        "isis.core.meta-model.validator.explicit-logical-type-names");
            }

            });

    }

    public static boolean check(final ObjectSpecification objectSpec) {
            //TODO
            // as a special case, don't enforce this for fixture scripts...
            // we never invoke actions on fixture scripts anyway

        if(objectSpec.isAbstract()) {
            return false; //skip validation
        }
        if (objectSpec.isEntity()) {
            return true;
        }
        if (objectSpec.isViewModel()) {
            //final ViewModelFacet viewModelFacet = objectSpec.getFacet(ViewModelFacet.class);
            // don't check JAXB DTOs
            final XmlType xmlType = objectSpec.getCorrespondingClass().getAnnotation(XmlType.class);
            if(xmlType != null) {
                return false; //skip validation
            }
            return true;
        }
        if(objectSpec.isMixin()) {
            return false; //skip validation
        }
        if (objectSpec.isManagedBean()) {
            // only check if domain service is contributing to the public API (UI/REST)
            if(!DomainServiceFacet.getNatureOfService(objectSpec).isPresent()) {
                return false; //skip validation
            }

            // don't check if domain service has only programmatic methods
            return objectSpec.streamAnyActions(MixedIn.INCLUDED).count()>0L;

        }
        return false; //skip validation
    }



}
