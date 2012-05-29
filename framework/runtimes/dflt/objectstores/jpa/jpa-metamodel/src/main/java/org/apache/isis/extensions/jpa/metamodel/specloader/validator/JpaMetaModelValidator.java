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
package org.apache.isis.extensions.jpa.metamodel.specloader.validator;

import java.text.MessageFormat;
import java.util.Collection;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.isis.extensions.jpa.metamodel.facets.object.discriminator.JpaDiscriminatorFacet;
import org.apache.isis.extensions.jpa.metamodel.facets.object.embeddable.JpaEmbeddableFacet;
import org.apache.isis.extensions.jpa.metamodel.facets.object.entity.JpaEntityFacet;
import org.apache.isis.extensions.jpa.metamodel.util.JpaPropertyUtils;

public class JpaMetaModelValidator extends MetaModelValidatorAbstract {

    public void validate() {
        try {
            ensureFoundAnnotatedEntities();
            ensureAllSpecificationsValid();
        } catch (final ClassNotFoundException e) {
            throw new MetaModelInvalidException(e);
        }
    }

    private void ensureFoundAnnotatedEntities() {
        final Collection<ObjectSpecification> objectSpecs = getSpecificationLoader().allSpecifications();
        for (final ObjectSpecification objectSpec : objectSpecs) {
            if (objectSpec.containsFacet(JpaEntityFacet.class)) {
                return;
            }
        }
        throw new MetaModelInvalidException("No annotated entities found; " + "are they annotated with @Entity? " + "are the entities referenced by the registered services? " + "are all services registered? " + "are you using the JPA reflector");
    }

    private void ensureAllSpecificationsValid() throws ClassNotFoundException {
        final Collection<ObjectSpecification> objectSpecs = getSpecificationLoader().allSpecifications();
        for (final ObjectSpecification objSpec : objectSpecs) {
            ensureNotAnnotatedAsBothEntityAndEmbeddable(objSpec);
            ensureEntityIfAnnotatedAsSuchHasOrInheritsAnIdProperty(objSpec);
            ensureEntityIfAnnotatedAsSuchAndConcreteHasDiscriminatorFacet(objSpec);
        }
    }

    private void ensureNotAnnotatedAsBothEntityAndEmbeddable(final ObjectSpecification objSpec) {
        final JpaEntityFacet entityFacet = objSpec.getFacet(JpaEntityFacet.class);
        final JpaEmbeddableFacet embeddableFacet = objSpec.getFacet(JpaEmbeddableFacet.class);
        if (entityFacet == null || embeddableFacet == null) {
            return;
        }

        final String classFullName = objSpec.getFullIdentifier();
        throw new MetaModelInvalidException(MessageFormat.format("Class {0} is mapped as both @Entity and @Embeddable; " + "not supported", classFullName));
    }

    private void ensureEntityIfAnnotatedAsSuchHasOrInheritsAnIdProperty(final ObjectSpecification objSpec) throws ClassNotFoundException {
        if (!objSpec.containsFacet(JpaEntityFacet.class)) {
            return;
        }

        final String classFullName = objSpec.getFullIdentifier();
        final Class<?> cls = Class.forName(classFullName);

        if (cls.isInterface()) {
            // TODO: could possibly have a FacetFactory to check that the
            // @javax.persistence.Transient
            // annotation has been set on all properties? (see
            // http://opensource.atlassian.com/projects/hibernate/browse/ANN-9)
            return;
        }

        final OneToOneAssociation idProperty = JpaPropertyUtils.getIdPropertyFor(objSpec);
        if (idProperty == null) {
            throw new MetaModelInvalidException(MessageFormat.format("Class {0} does not have a single property with IdFacet", classFullName));
        }
    }

    private void ensureEntityIfAnnotatedAsSuchAndConcreteHasDiscriminatorFacet(final ObjectSpecification objSpec) {
        if (!objSpec.containsFacet(JpaEntityFacet.class)) {
            return;
        }
        if (objSpec.isAbstract() || objSpec.containsFacet(JpaDiscriminatorFacet.class)) {
            return;
        }
        final String classFullName = objSpec.getFullIdentifier();
        throw new MetaModelInvalidException(MessageFormat.format("OpenJpa object store requires that concrete class {0} mapped by @Entity " + "must also have an @DiscriminatorValue annotation", classFullName));
    }

}
