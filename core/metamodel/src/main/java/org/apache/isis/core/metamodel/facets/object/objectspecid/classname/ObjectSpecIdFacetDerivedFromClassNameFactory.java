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

package org.apache.isis.core.metamodel.facets.object.objectspecid.classname;

import java.util.Collections;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.ObjectSpecIdFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;

import lombok.val;

public class ObjectSpecIdFacetDerivedFromClassNameFactory
extends FacetFactoryAbstract
implements MetaModelRefiner, ObjectSpecIdFacetFactory {

    @Inject
    private ClassSubstitutorRegistry classSubstitutorRegistry =
            // default for testing purposes only, overwritten in prod
            new ClassSubstitutorRegistry(Collections.singletonList( new ClassSubstitutorDefault()));


    public ObjectSpecIdFacetDerivedFromClassNameFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }
    public ObjectSpecIdFacetDerivedFromClassNameFactory(ClassSubstitutorRegistry classSubstitutorRegistry) {
        this();
        this.classSubstitutorRegistry = classSubstitutorRegistry;
    }

    @Override
    public void process(final ProcessObjectSpecIdContext processClassContext) {
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        // don't trash existing facet
        if(facetHolder.containsNonFallbackFacet(ObjectSpecIdFacet.class)) {
            return;
        }
        val cls = processClassContext.getCls();
        val substitute = classSubstitutorRegistry.getSubstitution(cls);
        if(substitute.isNeverIntrospect()) {
            return;
        }
        val objectSpecIdFacet = createObjectSpecIdFacet(facetHolder, substitute.apply(cls));
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

        val shouldCheck = getConfiguration().getCore().getMetaModel().getValidator().isExplicitObjectType();
        if(!shouldCheck) {
            return;
        }

        programmingModel.addValidatorSkipManagedBeans(

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
                                "isis.core.meta-model.validator.explicit-object-type");
                    } 
                }
    
                private boolean skip(ObjectSpecification objectSpec) {
                    return !check(objectSpec);
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
            return objectSpec.streamActions(MixedIn.INCLUDED).count()>0L;

        }
        return false; //skip validation
    }

}
