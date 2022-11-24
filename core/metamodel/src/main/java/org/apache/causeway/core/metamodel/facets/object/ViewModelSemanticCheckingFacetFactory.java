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
package org.apache.causeway.core.metamodel.facets.object;

import javax.inject.Inject;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.val;

public class ViewModelSemanticCheckingFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public ViewModelSemanticCheckingFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        val cls = processClassContext.getCls();

        final boolean implementsViewModel = org.apache.causeway.applib.ViewModel.class.isAssignableFrom(cls);
        if(!implementsViewModel){
            return;
        }

        if(getConfiguration().getApplib().getAnnotation().getViewModel().getValidation().getSemanticChecking().isEnable()) {
            checkViewModelSemantics(processClassContext);
        }
    }

    // -- HELPER

    private void checkViewModelSemantics(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        final DomainObject domainObject = processClassContext.synthesizeOnType(DomainObject.class).orElse(null);
        final boolean annotatedWithDomainObject = domainObject != null;

        if(annotatedWithDomainObject
                && (domainObject.nature().isBean()
                        || domainObject.nature().isEntity())) {
            ValidationFailure.raise(
                    facetHolder.getSpecificationLoader(),
                    Identifier.classIdentifier(LogicalType.fqcn(cls)),
                    String.format(
                        "Inconsistent view model / domain object nature semantics; %1$s should not implement "
                        + "%2$s and be annotated with @%3$s specifying a nature that conflicts with view-model semantics",
                        cls.getName(),
                        org.apache.causeway.applib.ViewModel.class.getSimpleName(),
                        DomainObject.class.getSimpleName())
                    );
        }
    }

}
