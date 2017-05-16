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

package org.apache.isis.core.metamodel.facets.object.validating.mustsatisfyspec;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.annotation.MustSatisfy;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.progmodel.DeprecatedMarker;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;

/**
 * @deprecated
 */
@Deprecated
public class MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacetFactory extends FacetFactoryAbstract
        implements MetaModelValidatorRefiner, DeprecatedMarker {

    private final MetaModelValidatorForDeprecatedAnnotation validator = new MetaModelValidatorForDeprecatedAnnotation(MustSatisfy.class);

    public MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        final Facet facet = create(processClassContaxt.getCls(), processClassContaxt.getFacetHolder(), servicesInjector);
        FacetUtil.addFacet(validator.flagIfPresent(facet));
    }

    private Facet create(final Class<?> clazz, final FacetHolder holder, final ServicesInjector servicesInjector) {
        return create(Annotations.getAnnotation(clazz, MustSatisfy.class), holder, servicesInjector);
    }

    private static Facet create(
            final MustSatisfy annotation,
            final FacetHolder holder,
            final ServicesInjector servicesInjector) {
        if (annotation == null) {
            return null;
        }
        final Class<?>[] values = annotation.value();
        final List<Specification> specifications = new ArrayList<Specification>();
        for (final Class<?> value : values) {
            final Specification specification = newSpecificationElseNull(value);
            if (specification != null) {
                specifications.add(specification);
            }
        }
        return specifications.size() > 0 ? new MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacet(specifications, holder, servicesInjector) : null;
    }

    private static Specification newSpecificationElseNull(final Class<?> value) {
        if (!(Specification.class.isAssignableFrom(value))) {
            return null;
        }
        try {
            return (Specification) value.newInstance();
        } catch (final InstantiationException e) {
            return null;
        } catch (final IllegalAccessException e) {
            return null;
        }
    }

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(validator);
    }

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        IsisConfiguration configuration = servicesInjector.getConfigurationServiceInternal();
        validator.setConfiguration(configuration);
    }



}
