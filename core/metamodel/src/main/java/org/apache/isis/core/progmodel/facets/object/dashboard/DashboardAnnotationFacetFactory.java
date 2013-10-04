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

package org.apache.isis.core.progmodel.facets.object.dashboard;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Dashboard;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.dashboard.DashboardFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

public class DashboardAnnotationFacetFactory extends FacetFactoryAbstract implements MetaModelValidatorRefiner{

    public DashboardAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final Dashboard annotation = Annotations.getAnnotation(processClassContext.getCls(), Dashboard.class);
        FacetUtil.addFacet(create(annotation, processClassContext.getFacetHolder()));
    }

    private DashboardFacet create(final Dashboard annotation, final FacetHolder holder) {
        return annotation != null ? new DashboardFacetAnnotation(holder) : null;
    }

    /* (non-Javadoc)
     * @see org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner#refineMetaModelValidator(org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite, org.apache.isis.core.commons.config.IsisConfiguration)
     */
    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        metaModelValidator.add(new MetaModelValidatorVisiting(newValidatorVisitor()));
    }

    private Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.SummarizingVisitor() {

            private final List<String> annotated = Lists.newArrayList();
            
            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                if(objectSpec.containsFacet(DashboardFacet.class)) {
                    final String fullIdentifier = objectSpec.getFullIdentifier();
                    
                    // TODO: it would good to flag if the facet is found on any non-services, however
                    // ObjectSpecification.isService(...) can only be trusted once a PersistenceSession exists.
                    // this ought to be improved upon at some point...
                    annotated.add(fullIdentifier);
                }
                return true; // keep searching
            }

            @Override
            public void summarize(ValidationFailures validationFailures) {
                if(annotated.size()>1) {
                    final String nonServiceNamesStr = Joiner.on(", ").join(annotated);
                    validationFailures.add(
                            "Only one service can be specified as the dashboard; "
                            + "found DashboardFacet on these classes: %s", nonServiceNamesStr);
                }
            }
        };
    }
}
