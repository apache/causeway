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
package org.apache.isis.persistence.jdo.metamodel;

import javax.jdo.annotations.IdentityType;

import org.springframework.stereotype.Component;

import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.ignore.datanucleus.RemoveDatanucleusPersistableTypesFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.datanucleus.RemoveDnPrefixedMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.jdo.RemoveJdoEnhancementTypesFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.jdo.RemoveJdoPrefixedMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel.Marker;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.isis.persistence.jdo.metamodel.facets.object.datastoreidentity.JdoDatastoreIdentityAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.metamodel.facets.object.query.JdoQueryAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.metamodel.facets.object.version.JdoVersionAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.metamodel.facets.prop.column.BigDecimalDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.metamodel.facets.prop.column.MandatoryFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.metamodel.facets.prop.column.MaxLengthDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.metamodel.facets.prop.notpersistent.JdoNotPersistentAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.metamodel.facets.prop.primarykey.JdoPrimaryKeyAnnotationFacetFactory;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

import lombok.val;

@Component
public class JdoProgrammingModel implements MetaModelRefiner {

    //@Inject private IsisConfiguration config;

    @Override
    public void refineProgrammingModel(ProgrammingModel pm) {

        val step1 = ProgrammingModel.FacetProcessingOrder.C2_AFTER_METHOD_REMOVING;

        // come what may, we have to ignore the PersistenceCapable supertype.
        pm.addFactory(step1, RemoveJdoEnhancementTypesFacetFactory.class, Marker.JDO);
        // so we may as well also just ignore any 'jdo' prefixed methods here also.
        pm.addFactory(step1, RemoveJdoPrefixedMethodsFacetFactory.class, Marker.JDO);
        // Datanucleus
        pm.addFactory(step1, RemoveDatanucleusPersistableTypesFacetFactory.class, Marker.JDO);
        pm.addFactory(step1, RemoveDnPrefixedMethodsFacetFactory.class, Marker.JDO);


        val step2 = ProgrammingModel.FacetProcessingOrder.A2_AFTER_FALLBACK_DEFAULTS;

        pm.addFactory(step2, JdoPersistenceCapableAnnotationFacetFactory.class, Marker.JDO);
        pm.addFactory(step2, JdoDatastoreIdentityAnnotationFacetFactory.class, Marker.JDO);

        pm.addFactory(step2, JdoPrimaryKeyAnnotationFacetFactory.class, Marker.JDO);
        pm.addFactory(step2, JdoNotPersistentAnnotationFacetFactory.class, Marker.JDO);
        
        // breaks idea of logical-type-names having namespaces
        //pm.addFactory(step2, JdoDiscriminatorAnnotationFacetFactory.class, Marker.JDO);
        
        pm.addFactory(step2, JdoVersionAnnotationFacetFactory.class, Marker.JDO);

        pm.addFactory(step2, JdoQueryAnnotationFacetFactory.class, Marker.JDO);

        pm.addFactory(step2, BigDecimalDerivedFromJdoColumnAnnotationFacetFactory.class, Marker.JDO);
        pm.addFactory(step2, MaxLengthDerivedFromJdoColumnAnnotationFacetFactory.class, Marker.JDO);
        // must appear after JdoPrimaryKeyAnnotationFacetFactory (above)
        // and also MandatoryFacetOnPropertyMandatoryAnnotationFactory
        // and also PropertyAnnotationFactory
        pm.addFactory(step2, MandatoryFromJdoColumnAnnotationFacetFactory.class, Marker.JDO);


        // -- validators

        addValidatorToEnsureIdentityType(pm);
        addValidatorToCheckForUnsupportedAnnotations(pm);

    }
    
    // -- HELPER

    private void addValidatorToEnsureIdentityType(ProgrammingModel pm) {

        pm.addVisitingValidatorSkipManagedBeans(objSpec -> {

            final JdoPersistenceCapableFacet jpcf = objSpec.getFacet(JdoPersistenceCapableFacet.class);
            if(jpcf == null) {
                return;
            }
            final IdentityType identityType = jpcf.getIdentityType();
            if(identityType == IdentityType.APPLICATION) {
                // ok

            } else if(identityType == IdentityType.NONDURABLE) {
                // ok; for use with DN view objects (http://www.datanucleus.org/products/accessplatform_3_2/datastores/rdbms_views.html)

            } else if(identityType == IdentityType.DATASTORE || identityType == IdentityType.UNSPECIFIED) {

                // TODO: ensure that DATASTORE has recognised @DatastoreIdentity attribute

            } else {
                // in fact, at the time of writing there are no others, so this is theoretical in case there is
                // a future change to the JDO spec
                ValidationFailure.raiseFormatted(
                        objSpec,
                        "%s: is annotated with @PersistenceCapable but with an unrecognized identityType (%s)",
                        objSpec.getFullIdentifier(),
                        identityType);
            }

        }, Marker.JDO);

    }

    private void addValidatorToCheckForUnsupportedAnnotations(ProgrammingModel pm) {

        pm.addVisitingValidatorSkipManagedBeans(objSpec -> {
            if (objSpec.containsNonFallbackFacet(ParentedCollectionFacet.class) && !objSpec.containsNonFallbackFacet(CollectionFacet.class)) {
                ValidationFailure.raiseFormatted(
                        objSpec,
                        "%s: JDO/DataNucleus object store currently does not supported Aggregated or EmbeddedOnly annotations",
                        objSpec.getFullIdentifier());
            }
        }, Marker.JDO);

    }
}
