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
package org.apache.causeway.persistence.jdo.metamodel.facets.object.version;

import javax.inject.Inject;
import javax.jdo.annotations.Version;

import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.persistence.jdo.provider.entities.JdoFacetContext;

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
            guardAgainstAmbiguousVersion(processClassContext);
        }
    }

    private void guardAgainstAmbiguousVersion(
            final ProcessClassContext processClassContext) {

        val cls = processClassContext.getCls();

        val versionsFoundDirectly = _Maps.<Class<?>, Version>newLinkedHashMap();

        _Reflect.streamTypeHierarchy(cls, InterfacePolicy.EXCLUDE)
        .forEach(type->
            _Annotations.synthesizeDirect(type, Version.class)
                .ifPresent(versionDirect->versionsFoundDirectly.put(type, versionDirect)));

        if(versionsFoundDirectly.size()>1) {
            ValidationFailure.raiseFormatted(
                    processClassContext.getFacetHolder(),
                    "@Version annotation is ambiguous within a class hierarchy, there can be only one. "
                    + "Conflicting types are: %s",
                    versionsFoundDirectly.keySet());
        }
    }

}
