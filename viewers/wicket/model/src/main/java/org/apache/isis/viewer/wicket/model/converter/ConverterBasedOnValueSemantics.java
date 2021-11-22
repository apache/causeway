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
package org.apache.isis.viewer.wicket.model.converter;

import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;

import lombok.NonNull;
import lombok.Synchronized;
import lombok.val;

public class ConverterBasedOnValueSemantics<T>
implements
    IConverter<T>,
    HasCommonContext {

    private static final long serialVersionUID = 1L;

    private final Identifier featureIdentifier;
    private final ScalarRepresentation scalarRepresentation;
    private transient _Either<OneToOneAssociation, ObjectActionParameter> propOrParam;
    private transient IsisAppCommonContext commonContext;

    public ConverterBasedOnValueSemantics(
            final @NonNull ObjectFeature propOrParam,
            final @NonNull ScalarRepresentation scalarRepresentation) {
        this.scalarRepresentation = scalarRepresentation;
        this.propOrParam = propOrParam instanceof OneToOneAssociation // memoize
                ? _Either.left((OneToOneAssociation)propOrParam)
                : _Either.right((ObjectActionParameter)propOrParam);
        this.featureIdentifier = propOrParam.getFeatureIdentifier();
    }

    /**
     * Parameter {@code locale} is ignored!
     * @see IConverter#convertToObject(String, Locale)
     */
    @Override
    public final T convertToObject(final String text, final Locale locale) throws ConversionException {

        // guard against framework bugs
        if(scalarRepresentation.isViewing()) {
            throw _Exceptions.illegalArgument("Internal Error: "
                    + "cannot convert a rendering representation back to its value-type '%s' -> %s",
                        text,
                        featureIdentifier);
        }

        val feature = feature();
        val valueFacet = valueFacet();

        val context = valueFacet
                .createValueSemanticsContext(feature);

        try {
            return valueFacet.selectParserForFeatureElseFallback(feature)
                    .parseTextRepresentation(context, text);
        } catch (Exception e) {
            if(e instanceof ConversionException) {
                throw e;
            } else {
                throw new ConversionException(e.getMessage(), e);
            }
        }
    }

    /**
     * Parameter {@code locale} is ignored!
     * @see IConverter#convertToString(Object, Locale)
     */
    @Override
    public final String convertToString(final T value, final Locale locale) {

        val feature = feature();
        val valueFacet = valueFacet();

        val context = valueFacet
                .createValueSemanticsContext(feature);

        switch(scalarRepresentation) {
        case EDITING:
            return valueFacet.selectParserForFeatureElseFallback(feature)
                    .parseableTextRepresentation(context, value);
        case VIEWING:
            return propOrParam.fold(
                    prop->valueFacet.selectRendererForPropertyElseFallback(prop)
                            .simpleTextPresentation(context, value),
                    param->valueFacet.selectRendererForParameterElseFallback(param)
                            .simpleTextPresentation(context, value));
        }

        throw _Exceptions.unmatchedCase(scalarRepresentation);
    }

    public String getEditingPattern() {
        val feature = feature();
        val valueFacet = valueFacet();
        val context = valueFacet
                .createValueSemanticsContext(feature);
        return valueFacet.selectParserForFeatureElseFallback(feature)
                .getPattern(context);
    }

    // -- HELPER

    @Synchronized
    private ObjectFeature feature() {
        if(propOrParam==null) {
            val feature = getSpecificationLoader().loadFeature(featureIdentifier).orElse(null);
            this.propOrParam = (feature instanceof OneToOneAssociation)
                    ? _Either.left((OneToOneAssociation)feature)
                    : _Either.right(((ObjectActionParameter)feature));
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
