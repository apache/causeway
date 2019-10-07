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
import org.apache.isis.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;

public class JdoMetaModelValidator {
    
    public JdoMetaModelValidator(IsisConfiguration config, ProgrammingModel programmingModel) {
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
        });
            
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
        });
        
    }


}
