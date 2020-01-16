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
package org.apache.isis.core.metamodel.facets.object;


import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.RecreatableDomainObject;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.applib.annotation.ViewModelLayout;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForValidationFailures;

import lombok.val;


public class ViewModelSemanticCheckingFacetFactory extends FacetFactoryAbstract
implements MetaModelRefiner {


    public ViewModelSemanticCheckingFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    private final MetaModelValidatorForValidationFailures validator = 
            new MetaModelValidatorForValidationFailures();

    @Override
    public void setMetaModelContext(MetaModelContext metaModelContext) {
        super.setMetaModelContext(metaModelContext);
        validator.setMetaModelContext(metaModelContext);
    }
    
    @Override
    public void process(final ProcessClassContext processClassContext) {

        // disable by default
        final boolean enable = getConfiguration().getApplib().getAnnotation().getViewModel().getValidation().getSemanticChecking().isEnable();
        if(!enable) {
            return;
        }

        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        final DomainObjectLayout domainObjectLayout = Annotations.getAnnotation(cls, DomainObjectLayout.class);
        final ViewModelLayout viewModelLayout = Annotations.getAnnotation(cls, ViewModelLayout.class);
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final ViewModel viewModel = Annotations.getAnnotation(cls, ViewModel.class);

        final boolean implementsViewModel = org.apache.isis.applib.ViewModel.class.isAssignableFrom(cls);
        final boolean implementsRecreatableDomainObject = org.apache.isis.applib.RecreatableDomainObject.class.isAssignableFrom(cls);

        final boolean annotatedWithDomainObjectLayout = domainObjectLayout != null;
        final boolean annotatedWithViewModelLayout = viewModelLayout != null;
        final boolean annotatedWithDomainObject = domainObject != null;
        final boolean annotatedWithViewModel = viewModel != null;

        if(implementsViewModel && implementsRecreatableDomainObject) {
            validator.onFailure(
                    facetHolder,
                    Identifier.classIdentifier(cls),
                    "Inconsistent view model / domain object semantics; %s should not implement both %s and %s interfaces (implement one or the other)",
                    cls.getName(),
                    org.apache.isis.applib.ViewModel.class.getSimpleName(),
                    org.apache.isis.applib.RecreatableDomainObject.class.getSimpleName());

        }
        if(implementsViewModel && annotatedWithDomainObject) {
            validator.onFailure(
                    facetHolder,
                    Identifier.classIdentifier(cls),
                    "Inconsistent view model / domain object semantics; %1$s should not implement %2$s and be annotated with @%3$s (annotate with %4$s instead of %2$s, or implement %5s instead of %2$s)",
                    cls.getName(),
                    org.apache.isis.applib.ViewModel.class.getSimpleName(),
                    DomainObject.class.getSimpleName(),
                    ViewModel.class.getSimpleName(),
                    RecreatableDomainObject.class.getSimpleName()
                    );
        }
        if(implementsViewModel && annotatedWithDomainObjectLayout) {
            validator.onFailure(
                    facetHolder,
                    Identifier.classIdentifier(cls),
                    "Inconsistent view model / domain object semantics; %1$s should not implement %2$s and be annotated with @%3$s (annotate with @%4$s instead of %3$s, or implement %5$s instead of %2$s)",
                    cls.getName(),
                    org.apache.isis.applib.ViewModel.class.getSimpleName(),
                    DomainObjectLayout.class.getSimpleName(),
                    ViewModelLayout.class.getSimpleName(),
                    RecreatableDomainObject.class.getSimpleName());
        }


        if(annotatedWithViewModel && implementsRecreatableDomainObject) {
            validator.onFailure(
                    facetHolder,
                    Identifier.classIdentifier(cls),
                    "Inconsistent view model / domain object semantics; %1$s should not be annotated with @%2$s but implement @%3$s (implement %4$s instead of %3$s, or annotate with @%5$s with nature of %6s, %7s or %8s instead of annotating with @%2$s)",
                    cls.getName(),
                    org.apache.isis.applib.annotation.ViewModel.class.getSimpleName(),
                    org.apache.isis.applib.RecreatableDomainObject.class.getSimpleName(),
                    org.apache.isis.applib.ViewModel.class.getName(),
                    org.apache.isis.applib.annotation.DomainObject.class.getName(),
                    Nature.VIEW_MODEL,
                    Nature.INMEMORY_ENTITY,
                    Nature.EXTERNAL_ENTITY
                    );
        }
        if(annotatedWithViewModel && annotatedWithDomainObject) {
            validator.onFailure(
                    facetHolder,
                    Identifier.classIdentifier(cls),
                    "Inconsistent view model / domain object semantics; %1$s should not be annotated with both @%2$s and @%3$s (annotate with one or the other)",
                    cls.getName(),
                    org.apache.isis.applib.annotation.ViewModel.class.getSimpleName(),
                    org.apache.isis.applib.annotation.DomainObject.class.getSimpleName());

        }
        if(annotatedWithViewModel && annotatedWithDomainObjectLayout) {
            validator.onFailure(
                    facetHolder,
                    Identifier.classIdentifier(cls),
                    "Inconsistent view model / domain object semantics; %1$s should not be annotated with both @%2$s and @%3$s (annotate with @%4$s instead of @%3$s, or annotate with @%5$s instead of @%2$s)",
                    cls.getName(),
                    org.apache.isis.applib.annotation.ViewModel.class.getSimpleName(),
                    DomainObjectLayout.class.getSimpleName(),
                    ViewModelLayout.class.getSimpleName(),
                    DomainObject.class.getSimpleName());
        }

        if(annotatedWithViewModelLayout && implementsRecreatableDomainObject) {
            validator.onFailure(
                    facetHolder,
                    Identifier.classIdentifier(cls),
                    "Inconsistent view model / domain object semantics; %1$s should not be annotated with @%2$s but implement @%3$s (implement %4$s instead of %3$s, or annotate with %5$s instead of %2$s)",
                    cls.getName(),
                    org.apache.isis.applib.annotation.ViewModelLayout.class.getSimpleName(),
                    RecreatableDomainObject.class.getSimpleName(),
                    org.apache.isis.applib.ViewModel.class.getSimpleName(),
                    DomainObjectLayout.class.getSimpleName());
        }
        if(annotatedWithViewModelLayout && annotatedWithDomainObject) {
            validator.onFailure(
                    facetHolder,
                    Identifier.classIdentifier(cls),
                    "Inconsistent view model / domain object semantics; %1$s should not be annotated with @%2$s and also be annotated with @%3$s (annotate with @%4$s instead of @%3$s, or instead annotate with @%5$s instead of @%2$s)",
                    cls.getName(),
                    org.apache.isis.applib.annotation.ViewModelLayout.class.getSimpleName(),
                    DomainObject.class.getSimpleName(),
                    org.apache.isis.applib.annotation.ViewModel.class.getSimpleName(),
                    DomainObjectLayout.class.getSimpleName());
        }
        if(annotatedWithViewModelLayout && annotatedWithDomainObjectLayout) {
            validator.onFailure(
                    facetHolder,
                    Identifier.classIdentifier(cls),
                    "Inconsistent view model / domain object semantics; %1$s should not be annotated with both @%2$s and @%3$s (annotate with one or the other)",
                    cls.getName(),
                    org.apache.isis.applib.annotation.ViewModel.class.getSimpleName(),
                    DomainObjectLayout.class.getSimpleName(),
                    ViewModelLayout.class.getSimpleName());
        }

        if(     annotatedWithDomainObject &&
                (domainObject.nature() == Nature.NOT_SPECIFIED || domainObject.nature() == Nature.JDO_ENTITY) &&
                implementsRecreatableDomainObject) {
            validator.onFailure(
                    facetHolder,
                    Identifier.classIdentifier(cls),
                    "Inconsistent view model / domain object semantics; %1$s should not be annotated with @%2$s with nature of %3$s and also implement %4$s (specify a nature of %5$s, %6$s or %7$s)",
                    cls.getName(),
                    DomainObject.class.getSimpleName(),
                    domainObject.nature(),
                    org.apache.isis.applib.RecreatableDomainObject.class.getSimpleName(),
                    Nature.EXTERNAL_ENTITY, Nature.INMEMORY_ENTITY, Nature.VIEW_MODEL);
        }

    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.addValidator(validator);
    }


}
