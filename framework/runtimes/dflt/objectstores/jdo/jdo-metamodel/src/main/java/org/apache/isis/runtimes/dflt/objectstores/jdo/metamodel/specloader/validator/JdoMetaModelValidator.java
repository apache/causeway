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

import javax.jdo.annotations.IdentityType;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

public class JdoMetaModelValidator extends MetaModelValidatorComposite {

    public JdoMetaModelValidator() {
        final MetaModelValidatorVisiting.SummarizingVisitor ensurePersistenceCapables = new MetaModelValidatorVisiting.SummarizingVisitor(){

            private boolean found = false;
            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                return !(found = objectSpec.containsFacet(JdoPersistenceCapableFacet.class));
            }

            @Override
            public void summarize(ValidationFailures validationFailures) {
                if(!found) {
                    validationFailures.add("DataNucleus object store: no @PersistenceCapable found. " +
                            "(Are the entities referenced by the registered services? " + 
                            "are all services registered? " + 
                            "are you using the JDO programming model facets?)");
                }
            }
        };
        add(new MetaModelValidatorVisiting(ensurePersistenceCapables));

        MetaModelValidatorVisiting.Visitor ensureIdentityType = new MetaModelValidatorVisiting.Visitor(){
            @Override
            public boolean visit(ObjectSpecification objSpec, ValidationFailures validationFailures) {
                final JdoPersistenceCapableFacet jpcf = objSpec.getFacet(JdoPersistenceCapableFacet.class);
                if(jpcf == null) {
                    return true;
                }
                final IdentityType identityType = jpcf.getIdentityType();
                if(identityType != IdentityType.DATASTORE && identityType != IdentityType.UNSPECIFIED) {
                    validationFailures.add("DataNucleus object store: {0} must be annotated with @PersistenceCapable, with an identityType of either DATASTORE or UNSPECIFIED (has an identityType of {1})", objSpec.getFullIdentifier(), identityType);
                }
                
                return true;
                // TODO: ensure that DATASTORE has recognised @DatastoreIdentity attribute
            }};
            
        add(new MetaModelValidatorVisiting(ensureIdentityType));
    }

}
