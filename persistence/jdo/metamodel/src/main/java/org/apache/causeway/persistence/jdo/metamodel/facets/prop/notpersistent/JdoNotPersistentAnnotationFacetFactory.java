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
package org.apache.causeway.persistence.jdo.metamodel.facets.prop.notpersistent;

import javax.inject.Inject;
import javax.jdo.annotations.NotPersistent;
import javax.persistence.Transient;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.val;

public class JdoNotPersistentAnnotationFacetFactory
extends FacetFactoryAbstract {

    private final JdoFacetContext jdoFacetContext;

    @Inject
    public JdoNotPersistentAnnotationFacetFactory(
            final MetaModelContext mmc,
            final JdoFacetContext jdoFacetContext) {
        super(mmc, FeatureType.PROPERTIES_ONLY);
        this.jdoFacetContext = jdoFacetContext;
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        if(!processJdoAnnotations(processMethodContext)) {
            processJpaAnnotations(processMethodContext);
        }
    }

    // -- HELPER

    private boolean processJdoAnnotations(final ProcessMethodContext processMethodContext) {

        // only applies to JDO entities; ignore any view models
        final Class<?> cls = processMethodContext.getCls();
        if(!jdoFacetContext.isPersistenceEnhanced(cls)) {
            return false;
        }

        final NotPersistent annotation = processMethodContext.synthesizeOnMethod(NotPersistent.class)
                .orElse(null);

        if (annotation == null) {
            return false;
        }

        val facetHolder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new JdoNotPersistentFacetFromAnnotation(facetHolder));
        return true;
    }


    private void processJpaAnnotations(final ProcessMethodContext processMethodContext) {

        //XXX ideally we would process JPA annotations only if the type has an @Entity annotation

        final Transient annotation = processMethodContext.synthesizeOnMethod(Transient.class)
                .orElse(null);

        if (annotation == null) {
            return;
        }

        val facetHolder = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new JdoNotPersistentFacetFromAnnotation(facetHolder));

    }


}
