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

package org.apache.isis.metamodel.facets.object.objectspecid.classname;

import java.util.stream.Stream;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.ObjectSpecIdFacetFactory;
import org.apache.isis.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.services.classsubstitutor.ClassSubstitutor;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;

import lombok.Setter;
import lombok.val;

public class ObjectSpecIdFacetDerivedFromClassNameFactory
extends FacetFactoryAbstract
implements MetaModelRefiner, ObjectSpecIdFacetFactory {

    @Inject
    private ClassSubstitutor classSubstitutor = new ClassSubstitutorDefault(); // default for testing purposes only, overwritten in prod

    public ObjectSpecIdFacetDerivedFromClassNameFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessObjectSpecIdContext processClassContext) {
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        // don't trash existing facet
        if(facetHolder.containsNonFallbackFacet(ObjectSpecIdFacet.class)) {
            return;
        }
        final Class<?> cls = processClassContext.getCls();
        final Class<?> substitutedClass = classSubstitutor.getClass(cls);
        if(substitutedClass == null) {
            return;
        }

        final ObjectSpecIdFacet objectSpecIdFacet = createObjectSpecIdFacet(facetHolder, substitutedClass);
        FacetUtil.addFacet(objectSpecIdFacet);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        // now a no-op.
    }

    private static ObjectSpecIdFacet createObjectSpecIdFacet(
            final FacetHolder facetHolder, 
            final Class<?> substitutedClass) {
        
        val serviceId = getServiceId(facetHolder);
        val isService = serviceId!=null;

        if (isService) {
            return new ObjectSpecIdFacetDerivedFromIoCNamingStrategy(serviceId, facetHolder);
        }
        return new ObjectSpecIdFacetDerivedFromClassName(substitutedClass, facetHolder);
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
    public void refineProgrammingModel(ProgrammingModel programmingModel) {



        val shouldCheck = getConfiguration().getReflector().getValidator().isExplicitObjectType();
        if(!shouldCheck) {
            return;
        }

        programmingModel.addValidator(

            new MetaModelValidatorVisiting.Visitor() {
                
                @Override
                public boolean visit(
                        ObjectSpecification objectSpec,
                        MetaModelValidator validator) {
                    
                    validate(objectSpec, validator);
                    return true;
                }
    
                private void validate(
                        ObjectSpecification objectSpec,
                        MetaModelValidator validator) {
                    
                    if(skip(objectSpec)) {
                        return;
                    }
                    val objectSpecIdFacet = objectSpec.getFacet(ObjectSpecIdFacet.class);
                    if(objectSpecIdFacet instanceof ObjectSpecIdFacetDerivedFromClassName) {
                        validator.onFailure(
                                objectSpec,
                                objectSpec.getIdentifier(),
                                "%s: the object type must be specified explicitly ('%s' config property). "
                                        + "Defaulting the object type from the package/class/package name can lead "
                                        + "to data migration issues for apps deployed to production (if the class is "
                                        + "subsequently refactored). "
                                        + "Use @Discriminator, @DomainObject(objectType=...) or "
                                        + "@PersistenceCapable(schema=...) to specify explicitly.",
                                        objectSpec.getFullIdentifier(),
                                "isis.reflector.validator.explicit-object-type");
                    }
                }
    
                private boolean skip(ObjectSpecification objectSpec) {
                    return !check(objectSpec);
                }
            });

    }

    public static boolean check(final ObjectSpecification objectSpec) {
        if(objectSpec.isExcludedFromMetamodel()) {
            // as a special case, don't enforce this for fixture scripts... 
            // we never invoke actions on fixture scripts anyway
            return false; //skip validation
        }
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
            // don't check if domain service isn't a target in public API (UI/REST)
            final DomainServiceFacet domainServiceFacet = objectSpec.getFacet(DomainServiceFacet.class);
            if(domainServiceFacet != null) {
                if(domainServiceFacet.getNatureOfService() == NatureOfService.DOMAIN) {
                    return false; //skip validation
                }
            }

            // don't check if domain service has only programmatic methods
            final Stream<ObjectAction> objectActions = objectSpec.streamObjectActions(Contributed.INCLUDED);
            return objectActions.anyMatch(__->true); // return true if not empty

        }
        return false; //skip validation
    }

}
