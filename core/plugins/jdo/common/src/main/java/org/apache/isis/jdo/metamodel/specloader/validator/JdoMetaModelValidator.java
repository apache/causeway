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
package org.apache.isis.jdo.metamodel.specloader.validator;

import javax.jdo.annotations.IdentityType;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.jdo.metamodel.facets.object.datastoreidentity.JdoDatastoreIdentityAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.object.discriminator.JdoDiscriminatorAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.jdo.metamodel.facets.object.query.JdoQueryAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.object.version.JdoVersionAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.prop.column.BigDecimalDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.prop.column.MandatoryFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.prop.column.MaxLengthDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.prop.notpersistent.JdoNotPersistentAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.prop.primarykey.JdoPrimaryKeyAnnotationFacetFactory;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.progmodel.ProgrammingModel.Marker;

import lombok.val;

public class JdoMetaModelValidator {
    
    public JdoMetaModelValidator(IsisConfiguration config, ProgrammingModel programmingModel) {
        
        val facetProcessingOrder = 
                ProgrammingModel.FacetProcessingOrder.A2_AFTER_FALLBACK_DEFAULTS;
        
        programmingModel.addFactory(facetProcessingOrder, JdoPersistenceCapableAnnotationFacetFactory.class, Marker.JDO);
        programmingModel.addFactory(facetProcessingOrder, JdoDatastoreIdentityAnnotationFacetFactory.class, Marker.JDO);

        programmingModel.addFactory(facetProcessingOrder, JdoPrimaryKeyAnnotationFacetFactory.class, Marker.JDO);
        programmingModel.addFactory(facetProcessingOrder, JdoNotPersistentAnnotationFacetFactory.class, Marker.JDO);
        programmingModel.addFactory(facetProcessingOrder, JdoDiscriminatorAnnotationFacetFactory.class, Marker.JDO);
        programmingModel.addFactory(facetProcessingOrder, JdoVersionAnnotationFacetFactory.class, Marker.JDO);

        programmingModel.addFactory(facetProcessingOrder, JdoQueryAnnotationFacetFactory.class, Marker.JDO);

        programmingModel.addFactory(facetProcessingOrder, BigDecimalDerivedFromJdoColumnAnnotationFacetFactory.class, Marker.JDO);
        programmingModel.addFactory(facetProcessingOrder, MaxLengthDerivedFromJdoColumnAnnotationFacetFactory.class, Marker.JDO);
        // must appear after JdoPrimaryKeyAnnotationFacetFactory (above)
        // and also MandatoryFacetOnPropertyMandatoryAnnotationFactory
        // and also PropertyAnnotationFactory
        programmingModel.addFactory(facetProcessingOrder, MandatoryFromJdoColumnAnnotationFacetFactory.class, Marker.JDO);

        
        addValidatorToEnsureIdentityType(programmingModel);
        addValidatorToCheckForUnsupportedAnnotations(programmingModel);
    }

    private void addValidatorToEnsureIdentityType(ProgrammingModel programmingModel) {
        
        programmingModel.addValidator((objSpec, validationFailures) -> {
        
            final JdoPersistenceCapableFacet jpcf = objSpec.getFacet(JdoPersistenceCapableFacet.class);
            if(jpcf == null) {
                return true;
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
                validationFailures.add(
                        objSpec.getIdentifier(),
                        "%s: is annotated with @PersistenceCapable but with an unrecognized identityType (%s)",
                        objSpec.getFullIdentifier(),
                        identityType);
            }

            return true;
        }, Marker.JDO);
            
    }

    private void addValidatorToCheckForUnsupportedAnnotations(ProgrammingModel programmingModel) {
        
        programmingModel.addValidator((objSpec, validationFailures) -> {
                if (objSpec.containsDoOpFacet(ParentedCollectionFacet.class) && !objSpec.containsDoOpFacet(CollectionFacet.class)) {
                    validationFailures.add(
                            objSpec.getIdentifier(),
                            "%s: DataNucleus object store currently does not supported Aggregated or EmbeddedOnly annotations",
                            objSpec.getFullIdentifier());
                }
                return true;
        }, Marker.JDO);
        
    }


}
