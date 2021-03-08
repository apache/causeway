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

package org.apache.isis.persistence.jdo.metamodel.facets.object.version;

import javax.inject.Inject;
import javax.jdo.annotations.Version;

import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.Setter;

public class JdoVersionAnnotationFacetFactory 
extends FacetFactoryAbstract
implements MetaModelRefiner {
    
    @Inject @Setter private JdoFacetContext jdoFacetContext;
    
    public JdoVersionAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        // deliberately do NOT search superclasses/superinterfaces
        final Class<?> cls = processClassContext.getCls();

        // only applies to JDO entities; ignore any view models
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return;
        }

        final Version annotation = Annotations.getDeclaredAnnotation(cls, Version.class);
        if (annotation == null) {
            return;
        }
        FacetUtil.addFacet(new JdoVersionFacetFromAnnotation(processClassContext.getFacetHolder()));
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.addValidatorSkipManagedBeans(newValidatorVisitor());
    }

    Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, MetaModelValidator validator) {
                validate(objectSpec, validator);
                return true;
            }

            private void validate(ObjectSpecification objectSpec, MetaModelValidator validator) {
                if(!declaresVersionAnnotation(objectSpec)) {
                    return;
                }

                ObjectSpecification superclassSpec = objectSpec.superclass();
                while(superclassSpec != null) {
                    if(declaresVersionAnnotation(superclassSpec)) {
                        validator.onFailure(
                                objectSpec,
                                objectSpec.getIdentifier(),
                                "%s: cannot have @Version annotated on this subclass and any of its supertypes; superclass: %s",
                                objectSpec.getFullIdentifier(),
                                superclassSpec.getFullIdentifier() );
                        return;
                    }
                    superclassSpec = superclassSpec.superclass();
                }
            }

            private boolean declaresVersionAnnotation(ObjectSpecification objectSpec) {
                return Annotations.getDeclaredAnnotation(objectSpec.getCorrespondingClass(), Version.class)!=null;
            }
        };
    }




}
