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

package org.apache.isis.jdo.metamodel.facets.object.version;

import javax.jdo.annotations.Version;

import org.apache.isis.metamodel.JdoMetamodelUtil;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.metamodel.specloader.validator.ValidationFailures;

public class JdoVersionAnnotationFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner {

    public JdoVersionAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        // deliberately do NOT search superclasses/superinterfaces
        final Class<?> cls = processClassContext.getCls();

        // only applies to JDO entities; ignore any view models
        if(!JdoMetamodelUtil.isPersistenceEnhanced(cls)) {
            return;
        }

        final Version annotation = Annotations.getDeclaredAnnotation(cls, Version.class);
        if (annotation == null) {
            return;
        }
        FacetUtil.addFacet(new JdoVersionFacetFromAnnotation(processClassContext.getFacetHolder()));
    }


    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator) {
        metaModelValidator.add(new MetaModelValidatorVisiting(newValidatorVisitor()));
    }

    Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                validate(objectSpec, validationFailures);
                return true;
            }

            private void validate(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                if(!declaresVersionAnnotation(objectSpec)) {
                    return;
                }

                ObjectSpecification superclassSpec = objectSpec.superclass();
                while(superclassSpec != null) {
                    if(declaresVersionAnnotation(superclassSpec)) {
                        validationFailures.add(
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
