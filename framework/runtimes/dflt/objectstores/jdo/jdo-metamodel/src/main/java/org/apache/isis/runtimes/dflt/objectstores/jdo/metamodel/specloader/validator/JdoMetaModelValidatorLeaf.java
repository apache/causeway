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
package org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.specloader.validator;

import java.util.Collection;

import javax.jdo.annotations.IdentityType;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

public class JdoMetaModelValidatorLeaf extends MetaModelValidatorAbstract {

    public void validate(ValidationFailures validationFailures) {
        ensureFoundAnnotatedEntities(validationFailures);
        ensureAllSpecificationsValid(validationFailures);
    }

    private void ensureFoundAnnotatedEntities(ValidationFailures validationFailures) {
        final Collection<ObjectSpecification> objectSpecs = getSpecificationLoaderSpi().allSpecifications();
        for (final ObjectSpecification objectSpec : objectSpecs) {
            if (objectSpec.containsFacet(JdoPersistenceCapableFacet.class)) {
                return;
            }
        }
        validationFailures.add("DataNucleus object store: no @PersistenceCapable found. " +
                               "(Are the entities referenced by the registered services? " + 
                               "are all services registered? " + 
                               "are you using the JDO programming model facets?)");
    }

    private void ensureAllSpecificationsValid(ValidationFailures validationFailures) {
        final Collection<ObjectSpecification> objectSpecs = getSpecificationLoaderSpi().allSpecifications();
        for (final ObjectSpecification objSpec : objectSpecs) {
            
            final JdoPersistenceCapableFacet jpcf = objSpec.getFacet(JdoPersistenceCapableFacet.class);
            if(jpcf == null) {
                continue;
            }
            final IdentityType identityType = jpcf.getIdentityType();
            if(identityType != IdentityType.DATASTORE) {
                validationFailures.add("DataNucleus object store: {0} must be annotated with @PersistenceCapable, with an identityType of either DATASTORE (has an identityType of {1})", objSpec.getFullIdentifier(), identityType);
            }
            
            // TODO: ensure that DATASTORE has recognised @DatastoreIdentity attribute
        }
    }


}
