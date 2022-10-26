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
package org.apache.causeway.core.metamodel.interactions.managed;

import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.binding.Observable;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.binding._BindableAbstract;
import org.apache.causeway.commons.internal.binding._Bindables;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _BindingUtil {

    enum TargetFormat {
        TITLE,
        HTML,
        PARSABLE_TEXT;
        boolean requiresParser() {
            return this==PARSABLE_TEXT;
        }
        boolean requiresRenderer() {
            return this==TITLE
                    || this==HTML;
        }
    }

    @SuppressWarnings({ "rawtypes" })
    Observable<String> bindAsFormated(
            final @NonNull TargetFormat format,
            final @NonNull OneToOneAssociation prop,
            final @NonNull _BindableAbstract<ManagedObject> bindablePropertyValue) {

        val spec = prop.getElementType();

        // value types should have associated parsers/formatters via value semantics
        return spec.valueFacet()
        .map(valueFacet->{
            val eitherRendererOrParser = format.requiresRenderer()
                ? Either.<Renderer, Parser>left(valueFacet.selectRendererForPropertyElseFallback(prop))
                : Either.<Renderer, Parser>right(valueFacet.selectParserForPropertyElseFallback(prop));
            val ctx = valueFacet.createValueSemanticsContext(prop);

            return bindAsFormated(format, spec, bindablePropertyValue, eitherRendererOrParser, ctx);
        })
        .orElseGet(()->
            // fallback Bindable that is floating free (unbound)
            // writing to it has no effect on the domain
            _Bindables.forValue(String.format("Could not find a ValueFacet for type %s",
                    spec.getLogicalType()))
        );

    }

    @SuppressWarnings({ "rawtypes" })
    Observable<String> bindAsFormated(
            final @NonNull TargetFormat format,
            final @NonNull ObjectActionParameter param,
            final @NonNull _BindableAbstract<ManagedObject> bindableParamValue) {

        guardAgainstNonScalarParam(param);

        val spec = param.getElementType();

        // value types should have associated parsers/formatters via value semantics
        return spec.valueFacet()
        .map(valueFacet->{
            val eitherRendererOrParser = format.requiresRenderer()
                ? Either.<Renderer, Parser>left(valueFacet.selectRendererForParameterElseFallback(param))
                : Either.<Renderer, Parser>right(valueFacet.selectParserForParameterElseFallback(param));
            val ctx = valueFacet.createValueSemanticsContext(param);

            return bindAsFormated(format, spec, bindableParamValue, eitherRendererOrParser, ctx);
        })
        .orElseGet(()->
            // fallback Bindable that is floating free (unbound)
            // writing to it has no effect on the domain
            _Bindables.forValue(String.format("Could not find a ValueFacet for type %s",
                    spec.getLogicalType()))
        );

    }

    // -- PREDICATES

    boolean hasParser(final @NonNull OneToOneAssociation prop) {
        return prop.getElementType()
                .valueFacet()
                .map(valueFacet->valueFacet.selectRendererForProperty(prop).isPresent())
                .orElse(false);
    }

    boolean hasParser(final @NonNull ObjectActionParameter param) {
        return isNonScalarParam(param)
                ? false
                : param.getElementType()
                    .valueFacet()
                    .map(valueFacet->valueFacet.selectRendererForParameter(param).isPresent())
                    .orElse(false);
    }

    // -- HELPER

    private boolean isNonScalarParam(final @NonNull ObjectActionParameter param) {
        return param.getFeatureType() == FeatureType.ACTION_PARAMETER_PLURAL;
    }

    private void guardAgainstNonScalarParam(final @NonNull ObjectActionParameter param) {
        if(isNonScalarParam(param)) {
            throw _Exceptions.illegalArgument(
                    "Non-scalar action parameters are neither parseable nor renderable: %s",
                    param.getFeatureIdentifier());
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Observable<String> bindAsFormated(
            final @NonNull TargetFormat format,
            final @NonNull ObjectSpecification spec,
            final @NonNull _BindableAbstract<ManagedObject> bindableValue,
            final @NonNull Either<Renderer, Parser> eitherRendererOrParser,
            final @NonNull ValueSemanticsProvider.Context context) {

        switch (format) {
        case TITLE: {
            val renderer = eitherRendererOrParser.left().orElseThrow();
            return bindableValue.map(value->{
                        val pojo = MmUnwrapUtil.single(value);
                        val title = renderer.titlePresentation(context, pojo);
                        return title;
                    });
        }
        case HTML: {
            val renderer = eitherRendererOrParser.left().orElseThrow();
            return bindableValue.map(value->{
                        val pojo = MmUnwrapUtil.single(value);
                        val html = renderer.htmlPresentation(context, pojo);
                        return html;
                    });
        }
        case PARSABLE_TEXT:
            val parser = eitherRendererOrParser.right().orElseThrow();
            return bindableValue.mapToBindable(
                    value->{
                        val pojo = MmUnwrapUtil.single(value);
                        val text = parser.parseableTextRepresentation(context, pojo);
                        //System.err.printf("toText: %s -> '%s'%n", ""+value, text);
                        return text;
                    },
                    text->{
                        val value = ManagedObject.value(spec, parser.parseTextRepresentation(context, text));
                        //System.err.printf("fromText: '%s' -> %s%n", text, ""+value);
                        return value;
                    });
        default:
            throw _Exceptions.unmatchedCase(format);
        }
    }


}
