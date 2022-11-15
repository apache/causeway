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
package org.apache.causeway.persistence.jdo.metamodel;

import javax.jdo.annotations.IdentityType;

import org.springframework.stereotype.Component;

import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.facets.object.ignore.datanucleus.RemoveDatanucleusPersistableTypesFacetFactory;
import org.apache.causeway.core.metamodel.facets.object.ignore.datanucleus.RemoveDnPrefixedMethodsFacetFactory;
import org.apache.causeway.core.metamodel.facets.object.ignore.jdo.RemoveJdoEnhancementTypesFacetFactory;
import org.apache.causeway.core.metamodel.facets.object.ignore.jdo.RemoveJdoPrefixedMethodsFacetFactory;
import org.apache.causeway.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel.Marker;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.persistence.jdo.metamodel.facets.object.datastoreidentity.JdoDatastoreIdentityAnnotationFacetFactory;
import org.apache.causeway.persistence.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacetFactory;
import org.apache.causeway.persistence.jdo.metamodel.facets.object.query.JdoQueryAnnotationFacetFactory;
import org.apache.causeway.persistence.jdo.metamodel.facets.object.version.JdoVersionAnnotationFacetFactory;
import org.apache.causeway.persistence.jdo.metamodel.facets.prop.column.BigDecimalFromColumnAnnotationFacetFactory;
import org.apache.causeway.persistence.jdo.metamodel.facets.prop.column.MandatoryFromColumnAnnotationFacetFactory;
import org.apache.causeway.persistence.jdo.metamodel.facets.prop.column.MaxLengthFromJdoColumnAnnotationFacetFactory;
import org.apache.causeway.persistence.jdo.metamodel.facets.prop.notpersistent.JdoNotPersistentAnnotationFacetFactory;
import org.apache.causeway.persistence.jdo.metamodel.facets.prop.primarykey.JdoPrimaryKeyAnnotationFacetFactory;
import org.apache.causeway.persistence.jdo.provider.entities.JdoFacetContext;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

import lombok.val;

@Component
public class JdoProgrammingModel implements MetaModelRefiner {

    //@Inject private CausewayConfiguration config;

    @Override
    public void refineProgrammingModel(final ProgrammingModel pm) {

        val step1 = ProgrammingModel.FacetProcessingOrder.C2_AFTER_METHOD_REMOVING;
        val mmc = pm.getMetaModelContext();

        // come what may, we have to ignore the PersistenceCapable supertype.
        pm.addFactory(step1, new RemoveJdoEnhancementTypesFacetFactory(mmc), Marker.JDO);
        // so we may as well also just ignore any 'jdo' prefixed methods here also.
        pm.addFactory(step1, new RemoveJdoPrefixedMethodsFacetFactory(mmc), Marker.JDO);
        // Datanucleus
        pm.addFactory(step1, new RemoveDatanucleusPersistableTypesFacetFactory(mmc), Marker.JDO);
        pm.addFactory(step1, new RemoveDnPrefixedMethodsFacetFactory(mmc), Marker.JDO);


        val step2 = ProgrammingModel.FacetProcessingOrder.A2_AFTER_FALLBACK_DEFAULTS;
        val jdoFacetContext = mmc.getServiceRegistry().lookupServiceElseFail(JdoFacetContext.class);

        pm.addFactory(step2, new JdoPersistenceCapableFacetFactory(mmc, jdoFacetContext), Marker.JDO);
        pm.addFactory(step2, new JdoDatastoreIdentityAnnotationFacetFactory(mmc, jdoFacetContext), Marker.JDO);

        pm.addFactory(step2, new JdoPrimaryKeyAnnotationFacetFactory(mmc, jdoFacetContext), Marker.JDO);
        pm.addFactory(step2, new JdoNotPersistentAnnotationFacetFactory(mmc, jdoFacetContext), Marker.JDO);

        // breaks idea of logical-type-names having namespaces
        //pm.addFactory(step2, JdoDiscriminatorAnnotationFacetFactory.class, Marker.JDO);

        pm.addFactory(step2, new JdoVersionAnnotationFacetFactory(mmc, jdoFacetContext), Marker.JDO);

        pm.addFactory(step2, new JdoQueryAnnotationFacetFactory(mmc, jdoFacetContext), Marker.JDO);

        pm.addFactory(step2, new BigDecimalFromColumnAnnotationFacetFactory(mmc), Marker.JDO);
        pm.addFactory(step2, new MaxLengthFromJdoColumnAnnotationFacetFactory(mmc), Marker.JDO);
        // must appear after JdoPrimaryKeyAnnotationFacetFactory (above)
        // and also MandatoryFacetOnPropertyMandatoryAnnotationFactory
        // and also PropertyAnnotationFactory
        pm.addFactory(step2, new MandatoryFromColumnAnnotationFacetFactory(mmc, jdoFacetContext), Marker.JDO);


        // -- validators

        addValidatorToEnsureIdentityType(pm);
        addValidatorToCheckForUnsupportedAnnotations(pm);

    }

    // -- HELPER

    private void addValidatorToEnsureIdentityType(final ProgrammingModel pm) {

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

    private void addValidatorToCheckForUnsupportedAnnotations(final ProgrammingModel pm) {

        pm.addVisitingValidatorSkipManagedBeans(objSpec -> {
            if (objSpec.containsNonFallbackFacet(ParentedCollectionFacet.class)
                    && !objSpec.containsNonFallbackFacet(CollectionFacet.class)) {
                ValidationFailure.raiseFormatted(
                        objSpec,
                        "%s: JDO/DataNucleus object store currently does not supported Aggregated or EmbeddedOnly annotations",
                        objSpec.getFullIdentifier());
            }
        }, Marker.JDO);

    }
}
