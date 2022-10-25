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

import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.commons.ScalarRepresentation;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;

import lombok.NonNull;
import lombok.val;

public class ConverterBasedOnValueSemantics<T>
extends ValueSemanticsModelAbstract
implements IConverter<T> {

    private static final long serialVersionUID = 1L;

    public ConverterBasedOnValueSemantics(
            final @NonNull ObjectFeature propOrParam,
            final @NonNull ScalarRepresentation scalarRepresentation) {
        super(propOrParam, scalarRepresentation);
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
                            .titlePresentation(context, value),
                    param->valueFacet.selectRendererForParameterElseFallback(param)
                            .titlePresentation(context, value));
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

    @Override
    protected ValueFacet<T> valueFacet() {
        return _Casts.uncheckedCast(super.valueFacet());
    }

}
