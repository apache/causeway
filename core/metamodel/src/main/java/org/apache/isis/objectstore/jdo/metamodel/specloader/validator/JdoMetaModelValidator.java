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
package org.apache.isis.objectstore.jdo.metamodel.specloader.validator;

import javax.jdo.annotations.IdentityType;

import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.parented.ParentedFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

public class JdoMetaModelValidator extends MetaModelValidatorComposite {

    public JdoMetaModelValidator() {
        addValidatorToEnsurePersistenceCapables();
        addValidatorToEnsureIdentityType();
        addValidatorToCheckForUnsupportedAnnotations();
    }

    private void addValidatorToEnsurePersistenceCapables() {
        final MetaModelValidatorVisiting.SummarizingVisitor ensurePersistenceCapables = new MetaModelValidatorVisiting.SummarizingVisitor(){

            private boolean found = false;
            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                boolean containsFacet = objectSpec.containsFacet(JdoPersistenceCapableFacet.class);
                if(containsFacet) {
                    found = true;
                    return false; // no need to keep searching
                }
                return true; // keep searching
            }

            @Override
            public void summarize(ValidationFailures validationFailures) {
                if(!found) {
                    validationFailures.add("No @PersistenceCapable entities found. " +
                            "(Are the entities referenced by the registered services? " + 
                            "are all services registered? " + 
                            "did the DataNucleus enhancer run?)");
                }
            }
        };
        add(new MetaModelValidatorVisiting(ensurePersistenceCapables));
    }

    private void addValidatorToEnsureIdentityType() {
        MetaModelValidatorVisiting.Visitor ensureIdentityType = new MetaModelValidatorVisiting.Visitor(){
            @Override
            public boolean visit(ObjectSpecification objSpec, ValidationFailures validationFailures) {
                final JdoPersistenceCapableFacet jpcf = objSpec.getFacet(JdoPersistenceCapableFacet.class);
                if(jpcf == null) {
                    return true;
                }
                final IdentityType identityType = jpcf.getIdentityType();
                if(identityType == IdentityType.APPLICATION) {
                    // ok
                    
                } else if(identityType == IdentityType.NONDURABLE) {
                    // ok; for use with view models (http://www.datanucleus.org/products/accessplatform_3_2/datastores/rdbms_views.html)
                    
                } else if(identityType == IdentityType.DATASTORE || identityType == IdentityType.UNSPECIFIED) {
                    
                    // TODO: ensure that DATASTORE has recognised @DatastoreIdentity attribute
                    
                } else {
                    // in fact, at the time of writing there are no others, so this is theoretical in case there is 
                    // a future change to the JDO spec
                    validationFailures.add(
                            "%s: is annotated with @PersistenceCapable but with an unrecognized identityType (%s)",
                            objSpec.getFullIdentifier(),
                            identityType);
                }
                
                return true;
            }};
            
        add(new MetaModelValidatorVisiting(ensureIdentityType));
    }

    private void addValidatorToCheckForUnsupportedAnnotations() {
        MetaModelValidatorVisiting.Visitor ensureIdentityType = new MetaModelValidatorVisiting.Visitor(){
            @Override
            public boolean visit(ObjectSpecification objSpec, ValidationFailures validationFailures) {
                if (objSpec.containsDoOpFacet(ParentedFacet.class) && !objSpec.containsDoOpFacet(CollectionFacet.class)) {
                    validationFailures.add(
                            "%s: DataNucleus object store currently does not supported Aggregated or EmbeddedOnly annotations",
                            objSpec.getFullIdentifier());
                }
                return true;
            }};
            
        add(new MetaModelValidatorVisiting(ensureIdentityType));
    }


}
