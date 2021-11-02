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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;

import lombok.Synchronized;
import lombok.val;

public abstract class ConverterBasedOnValueSemantics<T>
implements
    IConverter<T>,
    HasCommonContext {

    private static final long serialVersionUID = 1L;

    private final Identifier featureIdentifier;
    private final int paramIndex;
    private transient _Either<OneToOneAssociation,  ObjectActionParameter> propOrParam;
    private transient IsisAppCommonContext commonContext;

    protected ConverterBasedOnValueSemantics(final ObjectFeature propOrParam) {
        this.propOrParam = propOrParam instanceof OneToOneAssociation // memoize
                ? _Either.left((OneToOneAssociation)propOrParam)
                : _Either.right((ObjectActionParameter)propOrParam);
        this.featureIdentifier = propOrParam.getFeatureIdentifier();
        this.paramIndex = this.propOrParam.fold(
                prop->-1,
                param->param.getParameterIndex());
    }

    /**
     * Parameter {@code locale} is ignored!
     * @see IConverter#convertToObject(String, Locale)
     */
    @Override
    public final T convertToObject(final String text, final Locale locale) throws ConversionException {

        val feature = feature();
        val valueFacet = valueFacet();

        val context = valueFacet
                .createValueSemanticsContext(feature.getFeatureIdentifier());

        return valueFacet.selectParserForFeatureElseFallback(feature)
                .parseTextRepresentation(context, text);
    }

    /**
     * Parameter {@code locale} is ignored!
     * @see IConverter#convertToString(String, Locale)
     */
    @Override
    public final String convertToString(final T value, final Locale locale) {

        val feature = feature();
        val valueFacet = valueFacet();

        val context = valueFacet
                .createValueSemanticsContext(feature.getFeatureIdentifier());

        return valueFacet.selectParserForFeatureElseFallback(feature)
                .parseableTextRepresentation(context, value);
    }

    // -- HELPER

    @Synchronized
    private ObjectFeature feature() {
        if(propOrParam==null) {
            val typeSpec = getSpecificationLoader().specForLogicalTypeElseFail(featureIdentifier.getLogicalType());
            val member = typeSpec.getMemberElseFail(featureIdentifier.getMemberLogicalName());
            this.propOrParam = this.paramIndex<0
                    ? _Either.left((OneToOneAssociation)member)
                    : _Either.right(((ObjectAction)member).getParameters().getElseFail(paramIndex));
        }
        return propOrParam.fold(
                ObjectFeature.class::cast,
                ObjectFeature.class::cast);
    }

    @SuppressWarnings("unchecked")
    private ValueFacet<T> valueFacet() {

        val feature = feature();
        val valueFacet = feature.getElementType()
                .lookupFacet(ValueFacet.class)
                .orElseThrow(()->_Exceptions.noSuchElement(
                        "Value type Property or Parameter %s is missing a ValueFacet",
                        feature.getFeatureIdentifier()));

        return valueFacet;
    }

    // -- DEPENDENCIES

    @Override
    public final IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }

}
