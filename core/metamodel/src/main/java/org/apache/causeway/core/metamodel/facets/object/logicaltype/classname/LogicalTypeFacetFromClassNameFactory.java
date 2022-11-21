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
package org.apache.causeway.core.metamodel.facets.object.logicaltype.classname;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.causeway.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;

public class LogicalTypeFacetFromClassNameFactory
extends FacetFactoryAbstract
implements
        ObjectTypeFacetFactory,
        MetaModelRefiner {

    @Inject
    public LogicalTypeFacetFromClassNameFactory(
            final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    // -- JUNIT SUPPORT

    public static LogicalTypeFacetFromClassNameFactory forTesting(
            final MetaModelContext mmc) {
        return new LogicalTypeFacetFromClassNameFactory(mmc);
    }

    @Override
    public void process(final ProcessObjectTypeContext processClassContext) {
        // no-op.
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        // no-op.
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

            val logicalType = objectSpec.getLogicalType();

            //XXX has a slight chance to be a false positive; would need to check whether annotated with @Named
            if(logicalType.getClassName().equals(logicalType.getLogicalTypeName())) {
                ValidationFailure.raiseFormatted(
                        objectSpec,
                        "%s: the object type must be specified explicitly ('%s' config property). "
                                + "Defaulting the object type from the package/class/package name can lead "
                                + "to data migration issues for apps deployed to production (if the class is "
                                + "subsequently refactored). "
                                + "Use @Discriminator, @Named or "
                                + "@PersistenceCapable(schema=...) to specify explicitly.",
                        objectSpec.getFullIdentifier(),
                        "causeway.core.meta-model.validator.explicit-logical-type-names");
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
        if (objectSpec.isInjectable()) {
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
