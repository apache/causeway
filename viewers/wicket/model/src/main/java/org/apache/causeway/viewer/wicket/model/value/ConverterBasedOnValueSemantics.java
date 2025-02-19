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

import org.jspecify.annotations.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.commons.ViewOrEditMode;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;

import org.jspecify.annotations.NonNull;

public class ConverterBasedOnValueSemantics<T>
extends ValueSemanticsModelAbstract
implements IConverter<T> {

    private static final long serialVersionUID = 1L;

    public final Class<T> type;
    public final Class<?> resolvedType;

    public ConverterBasedOnValueSemantics(
            final @NonNull Class<T> type,
            final @NonNull ObjectFeature propOrParam,
            final @NonNull ViewOrEditMode scalarRepresentation) {
        super(propOrParam, scalarRepresentation);
        this.type = type;
        this.resolvedType = ClassUtils.resolvePrimitiveIfNecessary(propOrParam.getElementType().getCorrespondingClass());
        _Assert.assertTypeIsInstanceOf(resolvedType, type);
    }

    public final boolean canHandle(final @Nullable Class<?> aType) {
        return aType!=null
                && resolvedType.isAssignableFrom(ClassUtils.resolvePrimitiveIfNecessary(aType));
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

        var feature = feature();
        var valueFacet = valueFacet();

        var context = valueFacet
                .createValueSemanticsContext(feature);

        try {
            return valueFacet.selectParserForAttributeOrElseFallback(feature)
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

        var feature = feature();
        var valueFacet = valueFacet();

        var context = valueFacet
                .createValueSemanticsContext(feature);

        switch(scalarRepresentation) {
        case EDITING:
            return valueFacet.selectParserForAttributeOrElseFallback(feature)
                    .parseableTextRepresentation(context, value);
        case VIEWING:
            return propOrParam.fold(
                    prop->valueFacet.selectRendererForParamOrPropOrCollOrElseFallback(prop)
                            .titlePresentation(context, value),
                    param->valueFacet.selectRendererForParamOrPropOrCollOrElseFallback(param)
                            .titlePresentation(context, value));
        }

        throw _Exceptions.unmatchedCase(scalarRepresentation);
    }

    public String getEditingPattern() {
        var feature = feature();
        var valueFacet = valueFacet();
        var context = valueFacet
                .createValueSemanticsContext(feature);
        return valueFacet.selectParserForAttributeOrElseFallback(feature)
                .getPattern(context);
    }

    // -- HELPER

    @Override
    protected ValueFacet<T> valueFacet() {
        return _Casts.uncheckedCast(super.valueFacet());
    }

}
