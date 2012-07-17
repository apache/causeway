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

import java.text.MessageFormat;
import java.util.Collection;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.discriminator.JdoDiscriminatorFacet;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.embeddedonly.JdoEmbeddedOnlyFacet;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

public class JdoMetaModelValidator extends MetaModelValidatorAbstract {

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
            if (objectSpec.containsFacet(JdoPersistenceCapableFacet.class)) {
                return;
            }
        }
        throw new MetaModelInvalidException("No annotated entities found; " + "are they annotated with @PersistenceCapable? " + "are the entities referenced by the registered services? " + "are all services registered? " + "are you using the JDO reflector");
    }

    private void ensureAllSpecificationsValid() throws ClassNotFoundException {
        final Collection<ObjectSpecification> objectSpecs = getSpecificationLoader().allSpecifications();
        for (final ObjectSpecification objSpec : objectSpecs) {
            
            // TODO: seems that DataNucleus does require to be annotated as both.
            // ensureNotAnnotatedAsBothEntityAndEmbeddedOnly(objSpec);
            
            // there is no requirement to ensure that there is a primary key property;
            // can get the value directly from JDO
            
            ensureEntityIfAnnotatedAsSuchAndConcreteHasDiscriminatorFacet(objSpec);
        }
    }

    private void ensureNotAnnotatedAsBothEntityAndEmbeddedOnly(final ObjectSpecification objSpec) {
        final JdoPersistenceCapableFacet entityFacet = objSpec.getFacet(JdoPersistenceCapableFacet.class);
        final JdoEmbeddedOnlyFacet embeddableFacet = objSpec.getFacet(JdoEmbeddedOnlyFacet.class);
        if (entityFacet == null || embeddableFacet == null) {
            return;
        }

        final String classFullName = objSpec.getFullIdentifier();
        throw new MetaModelInvalidException(MessageFormat.format("Class {0} is mapped as both @PersistenceCapable and @EmbeddedOnly; " + "not supported", classFullName));
    }

    private void ensureEntityIfAnnotatedAsSuchAndConcreteHasDiscriminatorFacet(final ObjectSpecification objSpec) {
        if (!objSpec.containsFacet(JdoPersistenceCapableFacet.class)) {
            return;
        }
        if (objSpec.isAbstract() || objSpec.containsFacet(JdoDiscriminatorFacet.class)) {
            return;
        }
        final String classFullName = objSpec.getFullIdentifier();
        throw new MetaModelInvalidException(MessageFormat.format("DataNucleus object store requires that concrete class {0} mapped by @PersistenceCapable " + "must also have an @Discriminator annotation", classFullName));
    }

}
