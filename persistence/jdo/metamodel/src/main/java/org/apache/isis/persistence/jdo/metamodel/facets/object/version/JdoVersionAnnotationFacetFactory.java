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

import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.val;

public class JdoVersionAnnotationFacetFactory
extends FacetFactoryAbstract {

    private final JdoFacetContext jdoFacetContext;

    @Inject
    public JdoVersionAnnotationFacetFactory(
            final MetaModelContext mmc,
            final JdoFacetContext jdoFacetContext) {
        super(mmc, FeatureType.OBJECTS_ONLY);
        this.jdoFacetContext = jdoFacetContext;
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        val cls = processClassContext.getCls();

        // only applies to JDO entities; ignore any view models
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return;
        }

        val versionIfAny = processClassContext.synthesizeOnType(Version.class);
        FacetUtil.addFacetIfPresent(
                JdoVersionFacetFromAnnotation
                .create(versionIfAny, processClassContext.getFacetHolder()));

        if(versionIfAny.isPresent()) {
            guardAgainstVersionInAnySuper(processClassContext, cls.getSuperclass());
        }

    }

    private void guardAgainstVersionInAnySuper(
            final ProcessClassContext processClassContext,
            final Class<?> superclass) {
        if(superclass == null) {
            return;
        }
        val cls = processClassContext.getCls();
        val synth = _Annotations.synthesizeInherited(superclass, Version.class);
        if(synth.isPresent()) {
            ValidationFailure.raiseFormatted(
                    processClassContext.getFacetHolder(),
                    "%s: cannot have @Version annotated on this subclass and any of its supertypes; superclass: %s",
                    cls.getName(),
                    superclass.getName());
        }
        guardAgainstVersionInAnySuper(processClassContext, superclass.getSuperclass()); // recursive call

    }


}
