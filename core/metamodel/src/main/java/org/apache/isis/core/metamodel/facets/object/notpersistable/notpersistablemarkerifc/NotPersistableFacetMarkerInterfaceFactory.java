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

package org.apache.isis.core.metamodel.facets.object.notpersistable.notpersistablemarkerifc;

import org.apache.isis.applib.annotation.NotPersistable;
import org.apache.isis.applib.marker.NonPersistable;
import org.apache.isis.applib.marker.ProgramPersistable;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.notpersistable.NotPersistableFacet;
import org.apache.isis.core.metamodel.progmodel.DeprecatedMarker;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

/**
 * @deprecated
 */
@Deprecated
public class NotPersistableFacetMarkerInterfaceFactory extends FacetFactoryAbstract implements
        MetaModelValidatorRefiner, DeprecatedMarker {

    public NotPersistableFacetMarkerInterfaceFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final NotPersistable.By initiatedBy = NotPersistable.By.lookupForMarkerInterface(processClassContext.getCls());
        FacetUtil.addFacet(create(initiatedBy, processClassContext.getFacetHolder()));
    }

    private NotPersistableFacet create(final NotPersistable.By initiatedBy, final FacetHolder holder) {
        return initiatedBy != null ? new NotPersistableFacetMarkerInterface(initiatedBy, holder) : null;
    }

    @Override
    public void refineMetaModelValidator(
            final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(new MetaModelValidatorVisiting(newValidatorVisitor()));
    }

    private MetaModelValidatorVisiting.Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.Visitor() {
            @Override
            public boolean visit(
                    final ObjectSpecification objectSpec,
                    final ValidationFailures validationFailures) {

                if (objectSpec.containsDoOpFacet(NotPersistableFacet.class)) {
                    final NotPersistableFacet notPersistableFacet = objectSpec.getFacet(NotPersistableFacet.class);
                    if(notPersistableFacet instanceof NotPersistableFacetMarkerInterface) {
                        final NotPersistable.By by = ((NotPersistableFacetMarkerInterface) notPersistableFacet).value();
                        final Class<?> type =
                                by == NotPersistable.By.USER
                                    ? ProgramPersistable.class
                                    : NonPersistable.class;
                        validationFailures.add(String.format(
                                "%s implements %s; this interface is no longer supported",
                                objectSpec.getFullIdentifier(), type.getSimpleName()));
                    }
                }
                return true;
            }
        };
    }

}
