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
package org.apache.causeway.viewer.wicket.model.value;

import java.io.Serializable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.commons.ScalarRepresentation;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.util.WktContext;

import lombok.NonNull;
import lombok.Synchronized;
import lombok.val;

abstract class ValueSemanticsModelAbstract
implements
    HasCommonContext,
    Serializable {

    private static final long serialVersionUID = 1L;

    protected final Identifier featureIdentifier;
    protected final ScalarRepresentation scalarRepresentation;
    protected transient Either<OneToOneAssociation, ObjectActionParameter> propOrParam;


    protected ValueSemanticsModelAbstract(
            final @NonNull ObjectFeature propOrParam,
            final @NonNull ScalarRepresentation scalarRepresentation) {
        this.scalarRepresentation = scalarRepresentation;
        this.propOrParam = propOrParam instanceof OneToOneAssociation // memoize
                ? Either.left((OneToOneAssociation)propOrParam)
                : Either.right((ObjectActionParameter)propOrParam);
        this.featureIdentifier = propOrParam.getFeatureIdentifier();
    }

    // -- HELPER

    @Synchronized
    protected ObjectFeature feature() {
        if(propOrParam==null) {
            val feature = getSpecificationLoader().loadFeature(featureIdentifier).orElse(null);
            this.propOrParam = (feature instanceof OneToOneAssociation)
                    ? Either.left((OneToOneAssociation)feature)
                    : Either.right(((ObjectActionParameter)feature));
        }
        return propOrParam.fold(
                ObjectFeature.class::cast,
                ObjectFeature.class::cast);
    }

    protected ValueFacet<?> valueFacet() {
        val feature = feature();
        val valueFacet = feature.getElementType()
                .valueFacet()
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "Value type Property or Parameter %s is missing a ValueFacet",
                        feature.getFeatureIdentifier()));

        return valueFacet;
    }

    // -- DEPENDENCIES

    private transient MetaModelContext mmc;
    @Override
    public final MetaModelContext getMetaModelContext() {
        return mmc = WktContext.computeIfAbsent(mmc);
    }

}
