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

import java.util.List;

import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.ObjectSpecIdFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

public class ObjectSpecIdFacetDerivedFromClassNameFactory
        extends FacetFactoryAbstract
        implements MetaModelValidatorRefiner, ObjectSpecIdFacetFactory {

    public static final String ISIS_REFLECTOR_VALIDATOR_EXPLICIT_OBJECT_TYPE_KEY =
            "isis.reflector.validator.explicitObjectType";
    public static final boolean ISIS_REFLECTOR_VALIDATOR_EXPLICIT_OBJECT_TYPE_DEFAULT = false;


    private final ClassSubstitutor classSubstitutor = new ClassSubstitutor();

    public ObjectSpecIdFacetDerivedFromClassNameFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessObjectSpecIdContext processClassContext) {
        final FacetHolder facetHolder = processClassContext.getFacetHolder();
        // don't trash existing facet
        if(facetHolder.containsDoOpFacet(ObjectSpecIdFacet.class)) {
            return;
        }
        final Class<?> cls = processClassContext.getCls();
        final Class<?> substitutedClass = classSubstitutor.getClass(cls);

        final ObjectSpecIdFacet objectSpecIdFacet = createObjectSpecIdFacet(facetHolder, substitutedClass);
        FacetUtil.addFacet(objectSpecIdFacet);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        // now a no-op.
    }

    private static ObjectSpecIdFacet createObjectSpecIdFacet(
            final FacetHolder facetHolder, final Class<?> substitutedClass) {
        final boolean isService = isService(facetHolder);
        if (isService) {
            final String id = ServiceUtil.id(substitutedClass);
            if (id != null) {
                return new ObjectSpecIdFacetDerivedFromDomainServiceAnnotationElseGetId(id, facetHolder);
            }
        }
        return new ObjectSpecIdFacetDerivedFromClassName(substitutedClass, facetHolder);
    }

    private static boolean isService(final FacetHolder facetHolder) {
        if(facetHolder instanceof ObjectSpecification) {
            ObjectSpecification objectSpecification = (ObjectSpecification) facetHolder;
            return objectSpecification.isService();
        }
        return false;
    }

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {

        final boolean doCheck = configuration.getBoolean(
                ISIS_REFLECTOR_VALIDATOR_EXPLICIT_OBJECT_TYPE_KEY,
                ISIS_REFLECTOR_VALIDATOR_EXPLICIT_OBJECT_TYPE_DEFAULT);

        if(!doCheck) {
            return;
        }

        final MetaModelValidator validator = new MetaModelValidatorVisiting(
                new MetaModelValidatorVisiting.Visitor() {
                    @Override
                    public boolean visit(
                            final ObjectSpecification objectSpec,
                            final ValidationFailures validationFailures) {
                        validate(objectSpec, validationFailures);
                        return true;
                    }

                    private void validate(
                            final ObjectSpecification objectSpec,
                            final ValidationFailures validationFailures) {
                        if(skip(objectSpec)) {
                            return;
                        }
                        ObjectSpecIdFacet objectSpecIdFacet = objectSpec.getFacet(ObjectSpecIdFacet.class);
                        if(objectSpecIdFacet instanceof ObjectSpecIdFacetDerivedFromClassName &&
                                // as a special case, don't enforce this for fixture scripts... we never invoke actions on fixture scripts anyway
                                !FixtureScript.class.isAssignableFrom(objectSpec.getCorrespondingClass()) ) {

                            validationFailures.add(
                                    "%s: the object type must be specified explicitly ('%s' config property).  Defaulting the object type from the package/class/package name can lead to data migration issues for apps deployed to production (if the class is subsequently refactored).  Use @Discriminator, @DomainObject(objectType=...) or @PersistenceCapable(schema=...) to specify explicitly.",
                                    objectSpec.getFullIdentifier(), ISIS_REFLECTOR_VALIDATOR_EXPLICIT_OBJECT_TYPE_KEY);
                        }
                    }

                    private boolean skip(final ObjectSpecification objectSpec) {
                        return !check(objectSpec);
                    }


                });
        metaModelValidator.add(validator);
    }

    public static boolean check(final ObjectSpecification objectSpec) {
        if(objectSpec.isAbstract()) {
            return false;
        }
        if (objectSpec.isPersistenceCapable()) {
            return true;
        }
        if (objectSpec.isViewModel()) {
            final ViewModelFacet viewModelFacet = objectSpec.getFacet(ViewModelFacet.class);
            // don't check JAXB DTOs
            final XmlType xmlType = objectSpec.getCorrespondingClass().getAnnotation(XmlType.class);
            if(xmlType != null) {
                return false;
            }
            return true;
        }
        if(objectSpec.isMixin()) {
            return false;
        }
        if (objectSpec.isService()) {
            // don't check if domain service isn't a target in public API (UI/REST)
            final DomainServiceFacet domainServiceFacet = objectSpec.getFacet(DomainServiceFacet.class);
            if(domainServiceFacet != null) {
                if(domainServiceFacet.getNatureOfService() == NatureOfService.DOMAIN ||
                        domainServiceFacet.getNatureOfService() == NatureOfService.VIEW_CONTRIBUTIONS_ONLY) {
                    return false;
                }
            }

            // don't check if domain service has only programmatic methods
            final List<ObjectAction> objectActions = objectSpec.getObjectActions(Contributed.INCLUDED);
            if(objectActions.isEmpty()) {
                return false;
            }

            return true;
        }
        return false;
    }

}
